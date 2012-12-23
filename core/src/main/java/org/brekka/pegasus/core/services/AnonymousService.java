/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.List;

import org.brekka.paveway.core.model.CompletableFile;
import org.brekka.pegasus.core.model.AnonymousTransfer;
import org.brekka.pegasus.core.model.Dispatch;
import org.brekka.xml.pegasus.v2.model.DetailsType;

/**
 * @author Andrew Taylor
 * 
 */
public interface AnonymousService {

    AnonymousTransfer createTransfer(DetailsType details, Integer maxDownloads, Integer maxUnlockAttempts,
            List<CompletableFile> files, String code);

    AnonymousTransfer createTransfer(DetailsType details, Integer maxDownloads, Integer maxUnlockAttempts,
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
    AnonymousTransfer retrieveTransfer(String token);

}
