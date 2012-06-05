/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.List;

import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.pegasus.core.model.AnonymousTransfer;
import org.brekka.pegasus.core.model.Dispatch;
import org.brekka.xml.pegasus.v1.model.BundleType;

/**
 * @author Andrew Taylor
 *
 */
public interface AnonymousService {

    AnonymousTransfer createTransfer(String comment, String agreementText, 
            int maxDownloads, List<FileBuilder> fileBuilders);
    
    AnonymousTransfer createTransfer(String comment, String agreementText, 
            BundleType bundleType, Dispatch dispatch);
    
    AnonymousTransfer unlock(String token, String code);

    /**
     * @param token
     */
    void agreementAccepted(String token);

    /**
     * @param bundle
     * @return
     */
    boolean isAccepted(AnonymousTransfer anonymousTransfer);
    

}
