/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.io.InputStream;

import org.brekka.pegasus.core.model.AllocationFile;
import org.brekka.pegasus.core.model.Transfer;

/**
 * @author Andrew Taylor
 *
 */
public interface DownloadService {

    InputStream download(AllocationFile file, Transfer transfer, ProgressCallback progressCallback);
    
    interface ProgressCallback {
        void update(long current, long total);
    }
}
