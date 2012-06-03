/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.Date;

import org.brekka.pegasus.core.dao.AllocationDAO;
import org.brekka.pegasus.core.dao.AllocationFileDAO;
import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.model.AllocationFile;
import org.brekka.pegasus.core.services.AllocationService;
import org.brekka.xml.pegasus.v1.model.FileType;
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
     * @see org.brekka.pegasus.core.services.AllocationService#refreshAllocation(org.brekka.pegasus.core.model.AnonymousTransfer)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void refreshAllocation(Allocation allocation) {
        allocationDAO.refresh(allocation);
        assignFileXml(allocation);
    }
    
//    /* (non-Javadoc)
//     * @see org.brekka.pegasus.core.services.AllocationService#deallocateAllocation(org.brekka.pegasus.core.model.Allocation)
//     */
//    @Override
//    @Transactional(propagation=Propagation.REQUIRED)
//    public void deallocateAllocation(Allocation allocation) {
//        // Find the associated transfers and deallocate the keys in phalanx
//        List<AllocationList> transferList = allocationFileDAO.retrieveByAllocation(allocation);
//        for (Transfer transfer : transferList) {
//            UUID cryptedDataId = transfer.getCryptedDataId();
//            phalanxService.deleteCryptedData(new IdentityCryptedData(cryptedDataId));
//            transfer.setCryptedDataId(null);
//            transferDAO.update(transfer);
//        }
//    }
    
}
