/**
 *
 */
package org.brekka.pegasus.core.services;

import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.model.ListingCriteria;
import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.model.AllocationFile;
import org.brekka.pegasus.core.model.Dispatch;
import org.brekka.pegasus.core.model.FileDownloadEvent;
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

    void updateDetails(Allocation allocation);


    int retrieveDerivedFromListingRowCount(Dispatch derivedFrom);

    List<Allocation> retrieveDerivedFromListing(Dispatch derivedFrom, ListingCriteria listingCriteria);

    /**
     * Expire an allocation in a separate transaction.
     * @param transfer
     */
    void forceExpireAllocation(Allocation allocation);

    List<FileDownloadEvent> retrievePopulatedDownloadEvents(Allocation allocation);
}
