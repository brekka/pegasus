/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.List;
import java.util.UUID;

import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.pegasus.core.model.Deposit;
import org.brekka.pegasus.core.model.Dispatch;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.EMailAddress;
import org.brekka.pegasus.core.model.Inbox;
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
    Deposit createDeposit(Inbox inbox, String reference, 
            String comment, String agreementText, List<FileBuilder> fileBuilders);
    
    /**
     * 
     * @param inbox
     * @param reference
     * @param comment
     * @param agreementText
     * @param bundleType
     * @param dispatch
     * @return
     */
    Deposit createDeposit(Inbox inbox, String reference, String comment, String agreementText, 
            BundleType bundleType, Dispatch dispatch);

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
     * E-Mail address
     * @param eMailAddress
     * @return
     */
    Inbox retrieveForEMailAddress(EMailAddress eMailAddress);
    
    /**
     * Retrieve all deposits from the specified inbox.
     * @param inbox
     * @return
     */
    List<Deposit> retrieveDeposits(Inbox inbox);
    
    /**
     * Retrieve the specified deposit which will contain the file decryption key metadata.
     * @param deposit
     * @return
     */
    Deposit retrieveDeposit(UUID depositId);
    
    /**
     * @param keySafe
     * @return
     */
    List<Inbox> retrieveForKeySafe(KeySafe keySafe);

    /**
     * @param loopDivision
     * @return
     */
    List<Inbox> retrieveForDivision(Division division);


}
