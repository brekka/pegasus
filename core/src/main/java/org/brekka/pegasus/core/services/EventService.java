/**
 * 
 */
package org.brekka.pegasus.core.services;

import org.brekka.pegasus.core.model.Bundle;
import org.brekka.pegasus.core.model.BundleFile;
import org.brekka.pegasus.core.model.FileDownloadEvent;
import org.brekka.pegasus.core.model.Transfer;

/**
 * @author Andrew Taylor
 *
 */
public interface EventService {

    void bundleCreated(Bundle bundle);
    
    void transferUnlocked(Transfer transfer);
    
    FileDownloadEvent beginFileDownloadEvent(BundleFile bundleFile, Transfer transfer);
    
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
    int fileDownloadCount(BundleFile bundleFile, Transfer transfer);
}
