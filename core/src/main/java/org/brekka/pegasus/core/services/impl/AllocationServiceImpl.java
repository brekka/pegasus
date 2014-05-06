/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.brekka.pegasus.core.services.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.model.ListingCriteria;
import org.brekka.commons.persistence.support.EntityUtils;
import org.brekka.paveway.core.dao.CryptedFileDAO;
import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.pegasus.core.dao.AllocationDAO;
import org.brekka.pegasus.core.dao.AllocationFileDAO;
import org.brekka.pegasus.core.event.AllocationFileDeleteEvent;
import org.brekka.pegasus.core.model.AccessorContext;
import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.model.AllocationFile;
import org.brekka.pegasus.core.model.Dispatch;
import org.brekka.pegasus.core.model.FileDownloadEvent;
import org.brekka.pegasus.core.model.KeySafeAware;
import org.brekka.pegasus.core.model.XmlEntity;
import org.brekka.pegasus.core.services.AllocationService;
import org.brekka.pegasus.core.services.KeySafeService;
import org.brekka.xml.pegasus.v2.model.AllocationDocument;
import org.brekka.xml.pegasus.v2.model.BundleType;
import org.brekka.xml.pegasus.v2.model.FileType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
 * Providing operations for the base {@link Allocation} type.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Service
@Transactional
public class AllocationServiceImpl extends AllocationServiceSupport implements AllocationService {


    @Autowired
    private AllocationFileDAO allocationFileDAO;

    @Autowired
    private AllocationDAO allocationDAO;

    @Autowired
    private KeySafeService keySafeService;

    @Autowired
    private CryptedFileDAO cryptedFileDAO;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;


    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.BundleService#incrementDownloadCounter(org.brekka.pegasus.core.model.BundleFile)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW, isolation=Isolation.REPEATABLE_READ)
    public void incrementDownloadCounter(final AllocationFile allocationFile) {
        FileType xml = allocationFile.getXml();
        int maxDownloads = Integer.MAX_VALUE;
        if (xml.isSetMaxDownloads()) {
            maxDownloads = xml.getMaxDownloads();
        }
        AllocationFile managed = this.allocationFileDAO.retrieveById(allocationFile.getId());
        int downloadCount = managed.getDownloadCount();
        // Increment the downloads
        downloadCount++;
        managed.setDownloadCount(downloadCount);
        if (downloadCount == maxDownloads) {
            // Mark this file for deletion
            managed.setExpires(new Date());
        }
        this.allocationFileDAO.update(managed);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AllocationService#retrieveFile(java.util.UUID)
     */
    @Override
    @Transactional(readOnly=true)
    public AllocationFile retrieveFile(final UUID allocationFileId) {
        AccessorContext currentContext = AccessorContextImpl.getCurrent();
        AllocationFile unlockedAllocationFile = currentContext.retrieve(allocationFileId, AllocationFile.class);
        if (unlockedAllocationFile != null) {
            this.allocationFileDAO.refresh(unlockedAllocationFile);
            return unlockedAllocationFile;
        }

        AllocationFile allocationFile = this.allocationFileDAO.retrieveById(allocationFileId);
        if (allocationFile == null) {
            return null;
        }

        Allocation allocation = allocationFile.getAllocation();

        Allocation unlockedAllocation = currentContext.retrieve(allocation.getId(), Allocation.class);
        if (unlockedAllocation == null) {
            // Allocation has not yet been unlocked
            decryptDocument(allocation);
            currentContext.retain(allocation.getId(), allocation);
            unlockedAllocation = allocation;
        }
        allocationFile.setAllocation(unlockedAllocation);
        BundleType bundle = unlockedAllocation.getXml().getBean().getAllocation().getBundle();
        List<FileType> fileList = bundle.getFileList();
        for (FileType fileType : fileList) {
            if (allocationFile.getCryptedFile().getId().toString().equals(fileType.getUUID())) {
                allocationFile.setXml(fileType);
                break;
            }
        }
        currentContext.retain(allocationFile.getId(), allocationFile);
        return allocationFile;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AllocationService#deallocateAllocation(org.brekka.pegasus.core.model.Allocation)
     */
    @Override
    @Transactional(isolation=Isolation.SERIALIZABLE)
    public void clearAllocation(final Allocation allocation) {
        List<AllocationFile> fileList = this.allocationFileDAO.retrieveByAllocation(allocation);
        for (AllocationFile file : fileList) {
            clearAllocationFile(file, false);
        }

        XmlEntity<AllocationDocument> xml = allocation.getXml();

        allocation.setDeleted(new Date());
        allocation.setXml(null);
        this.allocationDAO.update(allocation);

        this.xmlEntityService.delete(xml.getId());
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AllocationService#releaseDetails(java.util.List)
     */
    @Override
    @Transactional(readOnly=true)
    public <T extends Allocation & KeySafeAware> void releaseDetails(final List<T> allocationList) {
        for (T allocation : allocationList) {
            if (allocation == null) {
                continue;
            }
            decryptDocument(allocation);
        }
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AllocationService#forceExpireAllocation(org.brekka.pegasus.core.model.Allocation)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW, isolation=Isolation.REPEATABLE_READ)
    public void forceExpireAllocation(final Allocation allocation) {
        Date expiryDate = new Date();
        // Update the incoming reference with the date
        allocation.setExpires(expiryDate);

        Allocation managed = this.allocationDAO.retrieveById(allocation.getId());
        managed.setExpires(expiryDate);

        this.allocationDAO.update(managed);
    }

    /**
     * @param file
     */
    @Override
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void clearAllocationFile(final AllocationFile file) {
        clearAllocationFile(file, true);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.DispatchService#updateDetails(org.brekka.pegasus.core.model.Dispatch, org.brekka.xml.pegasus.v2.model.DetailsType)
     */
    @Override
    @Transactional(isolation=Isolation.SERIALIZABLE)
    public void updateDetails(final Allocation allocation) {
        XmlEntity<AllocationDocument> updatedXml = allocation.getXml();
        Allocation latest = this.allocationDAO.retrieveById(allocation.getId());
        XmlEntity<AllocationDocument> updatedEntity = this.xmlEntityService.updateEntity(updatedXml, latest.getXml(), AllocationDocument.class);
        latest.setXml(updatedEntity);
        this.allocationDAO.update(latest);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AllocationService#retrieveDerivedFromListing(org.brekka.pegasus.core.model.Dispatch, org.brekka.commons.persistence.model.ListingCriteria)
     */
    @Override
    @Transactional(readOnly=true)
    public List<Allocation> retrieveDerivedFromListing(final Dispatch derivedFrom, final ListingCriteria listingCriteria) {
        List<Allocation> listing = this.allocationDAO.retrieveDerivedFromListing(derivedFrom, listingCriteria);
        return listing;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AllocationService#retrieveDerivedFromListingRowCount(org.brekka.pegasus.core.model.Dispatch)
     */
    @Override
    @Transactional(readOnly=true)
    public int retrieveDerivedFromListingRowCount(final Dispatch derivedFrom) {
        return retrieveDerivedFromListingRowCount(derivedFrom);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AllocationService#retrievePopulatedDownloadEvents(org.brekka.pegasus.core.model.Allocation)
     */
    @Override
    @Transactional(readOnly=true)
    public List<FileDownloadEvent> retrievePopulatedDownloadEvents(final Allocation allocation) {
        Allocation managed = this.allocationDAO.retrieveById(allocation.getId());
        Dispatch derivedFrom = managed.getDerivedFrom();
        decryptDocument(derivedFrom);
        List<FileDownloadEvent> downloads = this.eventService.retrieveFileDownloads(managed);

        List<AllocationFile> files = derivedFrom.getFiles();
        for (FileDownloadEvent fileDownloadEvent : downloads) {
            AllocationFile eventTransferFile = fileDownloadEvent.getTransferFile();
            for (AllocationFile dispatchTransferFile : files) {
                if (EntityUtils.identityEquals(dispatchTransferFile, eventTransferFile.getDerivedFrom())) {
                    // Copy XML to event transfer - just to provide the name. The UUID will be incorrect.
                    eventTransferFile.setXml(dispatchTransferFile.getXml());
                    break;
                }
            }
        }
        return downloads;
    }

    /**
     *
     * @param file
     * @param deleteAllocationIfPossible
     */
    protected void clearAllocationFile(final AllocationFile file, final boolean deleteAllocationIfPossible) {
        AllocationFile allocationFile = this.allocationFileDAO.retrieveById(file.getId());
        this.applicationEventPublisher.publishEvent(new AllocationFileDeleteEvent(allocationFile));

        if (allocationFile.getDeleted() != null) {
            // Already deleted
            return;
        }
        CryptedFile cryptedFile = allocationFile.getCryptedFile();
        List<AllocationFile> active = this.allocationFileDAO.retrieveActiveForCryptedFile(cryptedFile);
        boolean canDeleteCryptedFile = active.size() == 1;

        // Check whether we can delete the rest of the allocation also
        if (deleteAllocationIfPossible) {
            Allocation allocation = allocationFile.getAllocation();
            active = this.allocationFileDAO.retrieveActiveForAllocation(allocation);
            if (active.size() == 1) {
                // Make the allocation as expired. The reaper will pick it up soon
                allocation.setExpires(new Date());
                this.allocationDAO.update(allocation);
            }
        }
        allocationFile.setDeleted(new Date());
        allocationFile.setCryptedFile(null);
        this.allocationFileDAO.update(allocationFile);

        if (canDeleteCryptedFile) {
            // This is the only file. Safe to obliterate the crypted file
            this.pavewayService.removeFile(cryptedFile);
        }
    }
}
