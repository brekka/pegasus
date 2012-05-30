/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.List;

import javax.crypto.SecretKey;

import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.pegasus.core.model.Bundle;
import org.brekka.pegasus.core.model.BundleFile;
import org.brekka.pegasus.core.model.Transfer;
import org.joda.time.DateTime;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface BundleService {

    Bundle createBundle(String comment, String agreementText, String reference, DateTime expires, 
            int maxDownloads, SecretKey secretKey, int profile, List<FileBuilder> fileBuilders);
    
    void decryptTransfer(Transfer transfer, byte[] secretKeyBytes);
    
    void deallocateBundle(Bundle bundle);
    
    /**
     * Deallocate one of the files from a bundle.
     * @param bundleFile
     */
    void deallocateBundleFile(BundleFile bundleFile);

    /**
     * @param bundleFile
     */
    void incrementDownloadCounter(BundleFile bundleFile);

    /**
     * @param bundle
     */
    void refreshBundle(Bundle bundle);
}
