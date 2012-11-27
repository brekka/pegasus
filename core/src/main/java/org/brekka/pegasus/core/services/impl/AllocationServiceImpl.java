/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.brekka.pegasus.core.dao.AllocationDAO;
import org.brekka.pegasus.core.dao.AllocationFileDAO;
import org.brekka.pegasus.core.model.AccessorContext;
import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.model.AllocationFile;
import org.brekka.pegasus.core.services.AllocationService;
import org.brekka.phalanx.api.beans.IdentityCryptedData;
import org.brekka.xml.pegasus.v2.model.FileType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Service
@Transactional
public class AllocationServiceImpl extends AllocationServiceSupport implements AllocationService {

    
    @Autowired
    private AllocationFileDAO allocationFileDAO;
    
    @Autowired
    private AllocationDAO allocationDAO;
    
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.BundleService#incrementDownloadCounter(org.brekka.pegasus.core.model.BundleFile)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void incrementDownloadCounter(AllocationFile allocationFile) {
        FileType xml = allocationFile.getXml();
        int maxDownloads = Integer.MAX_VALUE;
        if (xml.isSetMaxDownloads()) {
            maxDownloads = xml.getMaxDownloads();
        }
        AllocationFile managed = allocationFileDAO.retrieveById(allocationFile.getId());
        int downloadCount = managed.getDownloadCount();
        // Increment the downloads
        downloadCount++;
        managed.setDownloadCount(downloadCount);
        if (downloadCount == maxDownloads) {
            // Mark this file for deletion
            managed.setExpires(new Date());
        }
        allocationFileDAO.update(managed);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AllocationService#retrieveFile(java.util.UUID)
     */
    @Override
    public AllocationFile retrieveFile(UUID allocationFileId) {
        AccessorContext currentContext = AccessorContext.getCurrent();
        AllocationFile allocationFile = currentContext.retrieve(allocationFileId, AllocationFile.class);
        if (allocationFile == null) {
            allocationFile = allocationFileDAO.retrieveById(allocationFileId);
            Allocation allocation = allocationFile.getAllocation();
            // TODO How do we unlock it?
            throw new IllegalStateException("Need to unlock allocation");
        }
        return allocationFile;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AllocationService#deallocateAllocation(org.brekka.pegasus.core.model.Allocation)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void clearAllocation(Allocation allocation) {
        UUID cryptedDataId = allocation.getCryptedDataId();
        
        List<AllocationFile> fileList = allocationFileDAO.retrieveByAllocation(allocation);
        for (AllocationFile file : fileList) {
            clearAllocationFile(file, false);
        }
        
        // Remove the crypted data from phalanx
        phalanxService.deleteCryptedData(new IdentityCryptedData(cryptedDataId));
        resourceStorageService.remove(allocation.getId());
        allocation.setDeleted(new Date());
        allocation.setCryptedDataId(null);
        allocationDAO.update(allocation);
    }

    /**
     * @param file
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void clearAllocationFile(AllocationFile file) {
        clearAllocationFile(file, true);
    }
    
    
    protected void clearAllocationFile(AllocationFile file, boolean deleteAllocationIfPossible) {
        if (file.getDeleted() != null) {
            // Already deleted
            return;
        }
        List<AllocationFile> active = allocationFileDAO.retrieveActiveForCryptedFile(file.getCryptedFileId());
        if (active.size() == 1) {
            // This is the only file. Safe to obliterate the crypted file
            UUID cryptedFileId = file.getCryptedFileId();
            pavewayService.removeFile(cryptedFileId); 
            file.setCryptedFileId(null);
        }
        // Check whether we can delete the rest of the allocation also
        if (deleteAllocationIfPossible) {
            Allocation allocation = file.getAllocation();
            active = allocationFileDAO.retrieveActiveForAllocation(allocation);
            if (active.size() == 1) {
                // Make the allocation as expired. The reaper will pick it up soon
                allocation.setExpires(new Date());
                allocationDAO.update(allocation);
            }
        }
        file.setDeleted(new Date());
        allocationFileDAO.update(file);
    }

//    /**
//     * Perform the de-allocation
//     * 
//     * @param bundle
//     */
//    @Override
//    @Transactional(propagation=Propagation.REQUIRED)
//    public void deallocateBundle(Bundle bundle) {
//        List<CryptedFile> fileList = cryptedFileDAO.retrieveByBundle(bundle);
//        for (CryptedFile bundleFile : fileList) {
//            // Bundle file id matches the crypted file id from paveway.
//            deallocateCryptedFile(bundleFile);
//        }
//        
//        // Clear the bundle XML
//        resourceStorageService.remove(bundle.getId());
//        bundleDAO.delete(bundle.getId());
//    }
//    
//    @Override
//    @Transactional(propagation=Propagation.REQUIRED)
//    public void deallocateCryptedFile(CryptedFile cryptedFile) {
//        cryptedFile = cryptedFileDAO.retrieveById(cryptedFile.getId());
//        List<CryptedPart> parts = cryptedFile.getParts();
//        for (CryptedPart cryptedPart : parts) {
//            UUID partId = cryptedPart.getId();
//            resourceStorageService.remove(partId);
//            cryptedPartDAO.delete(partId);
//        }
//        cryptedFileDAO.delete(cryptedFile.getId());
//    }
    
}
