/**
 * 
 */
package org.brekka.pegasus.core.services;

import org.brekka.paveway.core.model.UploadedFiles;
import org.brekka.pegasus.core.model.AnonymousTransfer;
import org.brekka.pegasus.core.model.Dispatch;
import org.brekka.pegasus.core.model.Token;
import org.brekka.xml.pegasus.v2.model.DetailsType;
import org.joda.time.DateTime;

/**
 * @author Andrew Taylor
 * 
 */
public interface AnonymousService {

    AnonymousTransfer createTransfer(Token token, DetailsType details, DateTime expires, Integer maxDownloads, Integer maxUnlockAttempts,
            UploadedFiles files, String code);

    AnonymousTransfer createTransfer(Token token, DetailsType details, DateTime expires, Integer maxDownloads, Integer maxUnlockAttempts,
            Dispatch dispatch, String code);

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

    /**
     * @param token
     * @return
     */
    AnonymousTransfer retrieveUnlockedTransfer(String token);
    
    /**
     * @param token
     * @return
     */
    AnonymousTransfer retrieveTransfer(String token);

    /**
     * @param transfer
     */
    void deleteTransfer(String token);
}
