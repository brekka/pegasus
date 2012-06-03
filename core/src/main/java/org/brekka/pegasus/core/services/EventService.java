/**
 * 
 */
package org.brekka.pegasus.core.services;

import org.brekka.pegasus.core.model.AllocationFile;
import org.brekka.pegasus.core.model.FileDownloadEvent;
import org.brekka.pegasus.core.model.Transfer;

/**
 * @author Andrew Taylor
 *
 */
public interface EventService {

    void transferCreated(Transfer transfer);
    
    void transferUnlocked(Transfer transfer);
    
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
}
