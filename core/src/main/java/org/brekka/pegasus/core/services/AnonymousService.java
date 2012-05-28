/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.List;

import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.pegasus.core.model.AllocatedBundle;
import org.brekka.pegasus.core.model.AnonymousTransfer;

/**
 * @author Andrew Taylor
 *
 */
public interface AnonymousService {

    AnonymousAllocatedBundle createBundle(String comment, String agreementText, List<FileBuilder> fileBuilders);
    
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
    
    
    interface AnonymousAllocatedBundle extends AllocatedBundle {
        String getToken();

        String getCode();
        
        String getFileName();
    }


}
