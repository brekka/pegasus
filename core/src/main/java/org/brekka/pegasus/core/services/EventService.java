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

    void bundleUnlocked(String remoteAddress, String onBehalfOfAddress, 
            String userAgent, Bundle bundle, Date agreementAccepted);
    
    FileDownloadEvent beginFileDownloadEvent(String remoteAddress, 
            String onBehalfOfAddress, String userAgent, UUID fileId);
    
    void completeEvent(FileDownloadEvent event);
}
