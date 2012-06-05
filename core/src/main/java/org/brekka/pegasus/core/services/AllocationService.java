/**
 * 
 */
package org.brekka.pegasus.core.services;

import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.model.AllocationFile;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface AllocationService {

    /**
     * @param bundleFile
     */
    void incrementDownloadCounter(AllocationFile allocationFile);

    /**
     * @param transfer
     */
    void refreshAllocation(Allocation allocation);
    
    void clearAllocation(Allocation allocation);
    
    void clearAllocationFile(AllocationFile file);
}
