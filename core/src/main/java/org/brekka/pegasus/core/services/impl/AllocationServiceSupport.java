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

import static org.brekka.pegasus.core.utils.PegasusUtils.checkNotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.support.EntityUtils;
import org.brekka.paveway.core.dao.CryptedFileDAO;
import org.brekka.paveway.core.model.CompletableUploadedFile;
import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.paveway.core.model.UploadedFiles;
import org.brekka.paveway.core.services.PavewayService;
import org.brekka.paveway.core.services.ResourceCryptoService;
import org.brekka.paveway.core.services.ResourceStorageService;
import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.dao.AllocationDAO;
import org.brekka.pegasus.core.dao.AllocationFileDAO;
import org.brekka.pegasus.core.model.AccessorContext;
import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.model.AllocationFile;
import org.brekka.pegasus.core.model.AnonymousTransfer;
import org.brekka.pegasus.core.model.Dispatch;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Token;
import org.brekka.pegasus.core.model.Transfer;
import org.brekka.pegasus.core.model.XmlEntity;
import org.brekka.pegasus.core.services.AllocationService;
import org.brekka.pegasus.core.services.EventService;
import org.brekka.pegasus.core.services.KeySafeService;
import org.brekka.pegasus.core.services.TokenService;
import org.brekka.pegasus.core.services.XmlEntityService;
import org.brekka.phalanx.api.services.PhalanxService;
import org.brekka.phoenix.api.services.CryptoProfileService;
import org.brekka.phoenix.api.services.SymmetricCryptoService;
import org.brekka.xml.pegasus.v2.model.AllocationDocument;
import org.brekka.xml.pegasus.v2.model.AllocationType;
import org.brekka.xml.pegasus.v2.model.BundleType;
import org.brekka.xml.pegasus.v2.model.DetailsType;
import org.brekka.xml.pegasus.v2.model.FileType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Methods common to all allocation services that should not be exposed via {@link AllocationService}.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
class AllocationServiceSupport {

    @Autowired
    protected PhalanxService phalanxService;

    @Autowired
    protected PavewayService pavewayService;

    @Autowired
    protected ResourceStorageService resourceStorageService;

    @Autowired
    protected SymmetricCryptoService symmetricCryptoService;

    @Autowired
    protected ResourceCryptoService resourceCryptoService;

    @Autowired
    protected CryptoProfileService cryptoProfileService;

    @Autowired
    protected EventService eventService;

    @Autowired
    protected KeySafeService keySafeService;

    @Autowired
    protected TokenService tokenService;

    @Autowired
    private AllocationDAO allocationDAO;

    @Autowired
    private AllocationFileDAO allocationFileDAO;

    @Autowired
    private CryptedFileDAO cryptedFileDAO;

    @Autowired
    protected XmlEntityService xmlEntityService;



    protected BundleType completeFiles(final int maxDownloads, final UploadedFiles files) {
        if (files == null) {
            return null;
        }
        BundleType bundleType = BundleType.Factory.newInstance();
        List<CompletableUploadedFile> ready = files.uploadComplete();
        if (ready.isEmpty()) {
            return null;
        }

        for (CompletableUploadedFile file : ready) {
            CryptedFile cryptedFile = pavewayService.complete(file);

            FileType fileXml = bundleType.addNewFile();
            fileXml.setName(cryptedFile.getFileName());
            fileXml.setMimeType(cryptedFile.getMimeType());
            fileXml.setUUID(cryptedFile.getId().toString());
            fileXml.setKey(cryptedFile.getSecretKey().getEncoded());
            fileXml.setLength(cryptedFile.getOriginalLength());
            if (maxDownloads > 0) {
                fileXml.setMaxDownloads(maxDownloads);
            }
        }
        return bundleType;
    }

    protected <T extends Allocation> T retrieveByToken(final String tokenStr, final Class<T> expectedType, final boolean notFoundThrows) {
        checkNotNull(tokenStr, "token");
        checkNotNull(expectedType, "expectedType");
        Token token = tokenService.retrieveByPath(tokenStr);
        T value = allocationDAO.retrieveByToken(token, expectedType);
        if (value == null
                && notFoundThrows) {
            throw new PegasusException(PegasusErrorCode.PG721,
                    "No %s found for token '%s'", expectedType.getSimpleName(), token);
        }
        return value;
    }


    /**
     * @param maxDownloads
     * @param bundleType
     * @return
     */
    protected BundleType copyBundle(final Integer maxDownloads, final BundleType bundleType) {
        if (bundleType == null) {
            return null;
        }
        BundleType b = BundleType.Factory.newInstance();
        List<FileType> fileList = bundleType.getFileList();
        for (FileType file : fileList) {
            FileType f = b.addNewFile();
            f.setKey(file.getKey());
            f.setLength(file.getLength());
            f.setMimeType(file.getMimeType());
            f.setName(file.getName());
            f.setUUID(file.getUUID());
            if (maxDownloads != null) {
                f.setMaxDownloads(maxDownloads);
            }
        }
        return b;
    }
    /**
     * @param dispatch
     * @return
     */
    protected BundleType copyDispatchBundle(final Dispatch dispatch, final Integer maxDownloads) {
        XmlEntity<AllocationDocument> xml = xmlEntityService.retrieveEntity(dispatch.getXml().getId(), AllocationDocument.class);
        AllocationType dispatchXml = xml.getBean().getAllocation();
        BundleType dispatchBundle = copyBundle(maxDownloads, dispatchXml.getBundle());
        return dispatchBundle;
    }

    /**
     * Prepare the XML based structure that will contain the details for this bundle,
     * while will be subsequently encrypted.
     * @param bundleType
     * @return
     */
    protected AllocationType prepareAllocationType(final BundleType bundleType, final DetailsType detailsType) {
        AllocationType allocationType = AllocationType.Factory.newInstance();
        if (bundleType != null) {
            allocationType.setBundle(bundleType);
        }
        if (detailsType != null) {
            allocationType.setDetails(detailsType);
        }
        return allocationType;
    }

    protected void decryptDocument(final Allocation allocation) {
        decryptDocument(allocation, null);
    }

    protected void decryptDocument(final Allocation allocation, final String password) {
        Allocation nAllocation = EntityUtils.narrow(allocation, Allocation.class);
        if (nAllocation == null) {
            return;
        }
        XmlEntity<AllocationDocument> existing = nAllocation.getXml();
        if (existing.getBean() != null) {
            // Already decrypted
            return;
        }
        boolean unlockSuccess = false;
        try {
            XmlEntity<AllocationDocument> xml;
            if (password == null) {
                xml = xmlEntityService.release(existing, AllocationDocument.class);
            } else {
                xml = xmlEntityService.release(existing, password, AllocationDocument.class);
            }
            nAllocation.setXml(xml);
            assignFileXml(nAllocation);
            unlockSuccess = true;
        } finally {
            if (nAllocation instanceof Transfer
                    && password != null) {
                eventService.transferUnlock((Transfer) nAllocation, unlockSuccess);
            }
        }

    }

    protected void encryptDocument(final Allocation allocation, final AllocationType allocationType, final KeySafe<?> keySafe) {
        AllocationDocument allocationDocument = AllocationDocument.Factory.newInstance();
        allocationDocument.setAllocation(allocationType);
        XmlEntity<AllocationDocument> xmlEntity = xmlEntityService.persistEncryptedEntity(allocationDocument, keySafe, true);
        allocation.setXml(xmlEntity);
    }

    protected void createAllocationFiles(final Allocation allocation) {
        XmlEntity<AllocationDocument> xml = xmlEntityService.release(allocation.getXml(), AllocationDocument.class);
        BundleType bundle = xml.getBean().getAllocation().getBundle();
        if (bundle == null) {
            return;
        }
        Dispatch derivedFrom = allocation.getDerivedFrom();
        List<AllocationFile> allocationFiles = new ArrayList<>();
        if (derivedFrom != null) {
            decryptDocument(derivedFrom);
            List<AllocationFile> files = derivedFrom.getFiles();
            for (AllocationFile source : files) {
                AllocationFile allocationFile = new AllocationFile();
                allocationFile.setAllocation(allocation);
                allocationFile.setCryptedFile(source.getCryptedFile());
                allocationFile.setXml(source.getXml());
                allocationFile.setDerivedFrom(source);
                allocationFileDAO.create(allocationFile);
                allocationFiles.add(allocationFile);
            }
        } else {
            List<FileType> fileList = bundle.getFileList();
            for (FileType fileType : fileList) {
                AllocationFile allocationFile = new AllocationFile();
                allocationFile.setAllocation(allocation);
                CryptedFile cryptedFile = cryptedFileDAO.retrieveById(UUID.fromString(fileType.getUUID()));
                allocationFile.setCryptedFile(cryptedFile);
                allocationFile.setXml(fileType);
                allocationFileDAO.create(allocationFile);
                allocationFiles.add(allocationFile);
            }
        }
        allocation.setFiles(allocationFiles);
    }


    protected void assignFileXml(final Allocation allocation) {
        XmlEntity<AllocationDocument> xml = xmlEntityService.release(allocation.getXml(), AllocationDocument.class);
        AllocationType allocationType = xml.getBean().getAllocation();
        List<AllocationFile> files = allocation.getFiles();
        BundleType bundle = allocationType.getBundle();
        if (bundle == null) {
            allocation.setFiles(Collections.<AllocationFile>emptyList());
            return;
        }
        List<FileType> fileList = bundle.getFileList();
        // Use nested loops as there should never be that many files.
        for (FileType fileType : fileList) {
            UUID cryptedFileID = UUID.fromString(fileType.getUUID());
            for (AllocationFile allocationFile : files) {
                UUID id = allocationFile.getCryptedFile().getId();
                if (id.equals(cryptedFileID)) {
                    allocationFile.setXml(fileType);
                    break; // Break out of this loop
                }
            }
        }
    }

    protected void bindToContext(final Allocation allocation) {
        bindToContext(allocation.getId(), allocation);
    }

    protected void bindToContext(final Serializable key, final Allocation allocation) {
        AccessorContext accessorContext = AccessorContextImpl.getCurrent();
        accessorContext.retain(key, allocation);
        List<AllocationFile> files = allocation.getFiles();
        for (AllocationFile allocationFile : files) {
            accessorContext.retain(allocationFile.getId(), allocationFile);
        }
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AllocationService#refreshAllocation(org.brekka.pegasus.core.model.AnonymousTransfer)
     */
    protected void refreshAllocation(final Allocation allocation) {
        Allocation nAllocation = EntityUtils.narrow(allocation, Allocation.class);
        if (nAllocation instanceof AnonymousTransfer) {
            AnonymousTransfer anonTrans = (AnonymousTransfer) nAllocation;
            // Need to keep the XML as we have no way to re-extract at this point (+ it should never change).
            XmlEntity<AllocationDocument> xml = anonTrans.getXml();
            allocationDAO.refresh(nAllocation);
            nAllocation.setXml(xml);
        } else {
            allocationDAO.refresh(nAllocation);
            xmlEntityService.release(nAllocation, AllocationDocument.class);
        }
        assignFileXml(nAllocation);
    }
}