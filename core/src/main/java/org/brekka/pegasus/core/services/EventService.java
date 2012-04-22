/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.Date;
import java.util.UUID;

import org.brekka.pegasus.core.model.Bundle;
import org.brekka.pegasus.core.model.FileDownloadEvent;

/**
 * @author Andrew Taylor
 *
 */
public interface EventService {

    void bundleCreated(Bundle bundle);
    
    void bundleUnlocked(Bundle bundle, Date agreementAccepted);
    
    FileDownloadEvent beginFileDownloadEvent(UUID fileId);
    
    void completeEvent(FileDownloadEvent event);
}
