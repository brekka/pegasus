/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.Collections;
import java.util.List;

import org.brekka.pegasus.core.dao.AllocationDAO;
import org.brekka.pegasus.core.dao.AllocationFileDAO;
import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.model.AllocationFile;
import org.brekka.pegasus.core.services.AllocationService;
import org.brekka.pegasus.core.services.ReaperService;
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
public class ReaperServiceImpl implements ReaperService {

    @Autowired
    private AllocationDAO allocationDAO;
    
    @Autowired
    private AllocationFileDAO allocationFileDAO;
    
    @Autowired
    private AllocationService allocationService;
    
    // TODO configured
    private int maxAllocationCount = 20;
    // TODO configured
    private int maxAllocationFileCount = 20;
    

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.ReaperService#clearAllocationFiles()
     */
    @Override
    @Scheduled(fixedDelay=5000) // Gap of five seconds between each invocation
    @Transactional(propagation=Propagation.REQUIRED)
    public void clearAllocationFiles() {
        List<AllocationFile> allocationFileList = Collections.emptyList();
        do {
            allocationFileList = allocationFileDAO.retrieveOldestExpired(maxAllocationFileCount);
            for (AllocationFile allocationFile : allocationFileList) {
                allocationService.clearAllocationFile(allocationFile);
            }
            // Keep looping until there are no more entries to expire
        } while (!allocationFileList.isEmpty());
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.ReaperService#clearAllocations()
     */
    @Override
    @Scheduled(fixedDelay=10000) // Gap of ten seconds between each invocation
    @Transactional(propagation=Propagation.REQUIRED)
    public void clearAllocations() {
        List<Allocation> allocationList = Collections.emptyList();
        do {
            allocationList = allocationDAO.retrieveOldestExpired(maxAllocationCount);
            for (Allocation allocation : allocationList) {
                allocationService.clearAllocation(allocation);
            }
            // Keep looping until there are no more entries to expire
        } while (!allocationList.isEmpty());
    }
}
