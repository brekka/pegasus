/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.List;
import java.util.UUID;

import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.model.AllocationFile;
import org.brekka.pegasus.core.model.KeySafeAware;

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
    
    <T extends Allocation & KeySafeAware> void releaseDetails(List<T> allocationList);
}
