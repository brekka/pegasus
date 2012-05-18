/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.List;

import org.brekka.pegasus.core.model.Invitation;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.xml.pegasus.v1.model.InvitationDocument;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface InvitationService {

    Invitation createInvitation(InvitationDocument document, Member recipient);
    
    List<Invitation> retrieveCurrent(Vault vault);
}
