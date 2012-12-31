/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.List;

import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.model.AllocationFile;
import org.brekka.pegasus.core.model.AnonymousTransfer;
import org.brekka.pegasus.core.model.FileDownloadEvent;
import org.brekka.pegasus.core.model.Transfer;

/**
 * @author Andrew Taylor
 *
 */
public interface EventService {

    void transferCreated(Transfer transfer);
    
    void transferUnlock(Transfer transfer, boolean success);
    
    FileDownloadEvent beginFileDownloadEvent(AllocationFile transferFile);
    
    void completeEvent(FileDownloadEvent event);
    
    void agreementAccepted(Transfer transfer);

    /**
     * @param transfer
     * @return
     */
    boolean isAccepted(Transfer transfer);

    /**
     * @param transfer
     * @return
     */
    int fileDownloadCount(AllocationFile transferFile, Transfer transfer);
    
    /**
     * Retrieve the number of failed attempts to unlock a file.
     * @param transfer
     * @return
     */
    int retrieveFailedUnlockAttempts(Transfer transfer);

    /**
     * Retrieve all file download events for the specified allocation.
     * @param allocation
     * @return
     */
    List<FileDownloadEvent> retrieveFileDownloads(Allocation allocation);
}
