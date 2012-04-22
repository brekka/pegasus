/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.Date;
import java.util.List;

import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.pegasus.core.model.AnonymousTransfer;
import org.brekka.xml.pegasus.v1.model.BundleType;

/**
 * @author Andrew Taylor
 *
 */
public interface AnonymousService {

    AnonymousTransfer createBundle(String comment, String code, List<FileBuilder> fileBuilders);
    
    BundleType unlock(String slug, String code, Date agreementAccepted, 
            String remoteAddress, String onBehalfOfAddress, String userAgent);
}
