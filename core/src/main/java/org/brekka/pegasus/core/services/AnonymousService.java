/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.List;

import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.pegasus.core.model.AnonymousTransfer;

/**
 * @author Andrew Taylor
 *
 */
public interface AnonymousService {

    AnonymousTransfer createTransfer(String comment, String agreementText, 
            int maxDownloads, List<FileBuilder> fileBuilders);
    
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
