/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.UUID;

import org.brekka.pegasus.core.model.Bundle;
import org.brekka.pegasus.core.model.FileDownloadEvent;
import org.brekka.pegasus.core.model.Transfer;

/**
 * @author Andrew Taylor
 *
 */
public interface EventService {

    void bundleCreated(Bundle bundle);
    
    void transferUnlocked(Transfer transfer);
    
    FileDownloadEvent beginFileDownloadEvent(UUID fileId);
    
    void completeEvent(FileDownloadEvent event);
    
    void agreementAccepted(Transfer transfer);

    /**
     * @param transfer
     * @return
     */
    boolean isAccepted(Transfer transfer);
}
