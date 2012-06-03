/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.List;

import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.model.Dispatch;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.KeySafe;
import org.joda.time.DateTime;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface DispatchService {

    /**
     * @param recipientEMail
     * @param division
     * @param keySafe
     * @param reference
     * @param comment
     * @param agreementText
     * @param fileBuilderList
     * @return
     */
    Allocation createDispatch(String recipientEMail, Division division, KeySafe keySafe, String reference,
            String comment, String agreementText, int maxDownloads, List<FileBuilder> fileBuilderList);
    
    

    /**
     * @param from
     * @param until
     * @return
     */
    List<Dispatch> retrieveCurrentForInterval(KeySafe keySafe, DateTime from, DateTime until);


}
