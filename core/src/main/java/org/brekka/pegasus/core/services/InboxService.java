/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.List;

import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.pegasus.core.model.Deposit;
import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.core.model.InboxTransferKey;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.xml.pegasus.v1.model.BundleType;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface InboxService {

    /**
     * Create a new inbox for the current user, using the specified vault.
     * @param token
     * @param keySafe
     * @return
     */
    Inbox createInbox(String name, String introduction, String inboxToken, KeySafe keySafe);
    
    /**
     * Create a deposit in the specified inbox. 
     * 
     * @param inboxToken
     * @param comment
     * @param fileBuilders
     * @return
     */
    InboxTransferKey depositFiles(Inbox inbox, String reference, 
            String comment, List<FileBuilder> fileBuilders);

    /**
     * Retrieve the inboxes owned by this member.
     * @return
     */
    List<Inbox> retrieveForMember();

    /**
     * @param inboxToken
     * @return
     */
    Inbox retrieveForToken(String inboxToken);
    
    /**
     * Retrieve all deposits from the specified inbox.
     * @param inbox
     * @return
     */
    List<Deposit> retrieveDeposits(Inbox inbox);
    
    /**
     * Unlock the specified deposit.
     * @param deposit
     * @return
     */
    BundleType unlock(Deposit deposit);
}
