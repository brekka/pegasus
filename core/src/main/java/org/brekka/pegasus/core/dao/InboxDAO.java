/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Token;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface InboxDAO extends EntityDAO<UUID, Inbox> {

    Inbox retrieveByToken(Token token);

    /**
     * @param member
     * @return
     */
    List<Inbox> retrieveForMember(Member member);
}
