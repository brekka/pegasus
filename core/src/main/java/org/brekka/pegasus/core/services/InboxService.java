/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.List;

import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.core.model.Vault;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface InboxService {

    /**
     * Create a new inbox for the current user, using the specified vault.
     * @param token
     * @param vault
     * @return
     */
    Inbox createInbox(String inboxToken, Vault vault);
    
    /**
     * Create a deposit in the specified inbox. 
     * 
     * @param inboxToken
     * @param comment
     * @param fileBuilders
     * @return
     */
    void depositFiles(Inbox inbox, String comment, List<FileBuilder> fileBuilders);

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
}
