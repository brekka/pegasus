/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.brekka.paveway.core.services.PavewayService;
import org.brekka.pegasus.core.dao.BundleDAO;
import org.brekka.pegasus.core.dao.BundleFileDAO;
import org.brekka.pegasus.core.dao.TransferDAO;
import org.brekka.pegasus.core.model.Bundle;
import org.brekka.pegasus.core.model.BundleFile;
import org.brekka.pegasus.core.model.Transfer;
import org.brekka.pegasus.core.services.BundleReaperService;
import org.brekka.phalanx.api.beans.IdentityCryptedData;
import org.brekka.phalanx.api.services.PhalanxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Transactional
@Service
public class BundleReaperServiceImpl implements BundleReaperService {

    // TODO configured
    private int maxBundleCount = 20;
    
    @Autowired
    private BundleDAO bundleDAO;
    
    @Autowired
    private BundleFileDAO bundleFileDAO;
    
    @Autowired
    private TransferDAO transferDAO;
    
    @Autowired
    private PavewayService pavewayService;
    
    @Autowired
    private PhalanxService phalanxService;
    
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.BundleReaperService#deallocate(int)
     */
    @Override
    @Scheduled(fixedDelay=10000) // Gap of ten seconds between each invocation
    @Transactional(propagation=Propagation.REQUIRED)
    public void deallocate() {
        List<Bundle> bundleList = bundleDAO.retrieveOldestExpired(maxBundleCount);
        for (Bundle bundle : bundleList) {
            deallocate(bundle);
        }
    }


    /**
     * Perform the de-allocation
     * 
     * @param bundle
     */
    protected void deallocate(Bundle bundle) {
        List<BundleFile> fileList = bundleFileDAO.retrieveByBundle(bundle);
        for (BundleFile bundleFile : fileList) {
            // Bundle file id matches the crypted file id from paveway.
            pavewayService.remove(bundleFile.getId());
            bundleDAO.delete(bundleFile.getId());
        }
        
        // Find the associated transfers and deallocate the keys in phalanx
        List<Transfer> transferList = transferDAO.retrieveByBundle(bundle);
        for (Transfer transfer : transferList) {
            UUID cryptedDataId = transfer.getCryptedDataId();
            phalanxService.deleteCryptedData(new IdentityCryptedData(cryptedDataId));
            transfer.setCryptedDataId(null);
            transferDAO.update(transfer);
        }
        
        // Mark as deleted, clear the IV
        bundle.setDeleted(new Date());
        bundle.setIv(null);
        bundleDAO.update(bundle);
    }

}
