/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.Member;

/**
 * @author Andrew Taylor
 *
 */
public interface MemberDAO extends EntityDAO<UUID, Member> {

    /**
     * @param openId
     * @return
     */
    Member retrieveByOpenId(String openId);

}
