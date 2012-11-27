/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.List;
import java.util.UUID;

import org.brekka.pegasus.core.model.Invitation;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.xml.pegasus.v2.model.InvitationDocument;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface InvitationService {

    Invitation createInvitation(InvitationDocument document, Member recipient, UUID invitedResourceCryptedDataId);
    
    List<Invitation> retrieveCurrent(Vault vault);
}
