/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.UUID;

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

    void clearAllocation(Allocation allocation);
    
    void clearAllocationFile(AllocationFile file);

    /**
     * @param fileId
     * @return
     */
    AllocationFile retrieveFile(UUID allocationFileId);
}
