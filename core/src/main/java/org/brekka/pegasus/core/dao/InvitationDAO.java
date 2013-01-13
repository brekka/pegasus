/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.Invitation;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Token;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 * 
 */
public interface InvitationDAO extends EntityDAO<UUID, Invitation> {

    /**
     * @param member
     * @param vault
     * @return
     */
    List<Invitation> retrieveForMember(Member member);

    Invitation retrieveByToken(Token token);
}
