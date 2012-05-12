/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.Date;
import java.util.List;

import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.pegasus.core.model.AllocatedBundle;
import org.brekka.xml.pegasus.v1.model.BundleType;

/**
 * @author Andrew Taylor
 *
 */
public interface AnonymousService {

    AnonymousAllocatedBundle createBundle(String comment, List<FileBuilder> fileBuilders);
    
    BundleType unlock(String token, String code, Date agreementAccepted);
    
    
    interface AnonymousAllocatedBundle extends AllocatedBundle {
        String getToken();

        String getCode();
        
        String getFileName();
    }
}
