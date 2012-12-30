/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.List;

import org.brekka.paveway.core.model.CompletableUploadedFile;
import org.brekka.paveway.core.model.UploadedFiles;
import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.model.Dispatch;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.xml.pegasus.v2.model.DetailsType;
import org.joda.time.DateTime;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface DispatchService {

    /**
     * 
     * @param keySafe
     * @param details
     * @param maxDownloads
     * @param fileBuilderList
     * @return
     */
    Dispatch createDispatch(KeySafe<?> keySafe, DetailsType details, DateTime expires, Integer maxDownloads, UploadedFiles files);
    
    

    /**
     * @param from
     * @param until
     * @return
     */
    List<Dispatch> retrieveCurrentForInterval(KeySafe<?> keySafe, DateTime from, DateTime until);



    /**
     * @param recipientEMail
     * @param division
     * @param keySafe
     * @param detailsType
     * @param maxDownloads
     * @param fileBuilderList
     * @return
     */
    Allocation createDispatchAndAllocate(String recipientEMail, Division<?> division, KeySafe<?> keySafe,
            DetailsType detailsType, DateTime dispatchExpires, DateTime allocationExpires, int maxDownloads, UploadedFiles files);


}
