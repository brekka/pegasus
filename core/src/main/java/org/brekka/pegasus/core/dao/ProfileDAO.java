/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Profile;
import org.brekka.pegasus.core.model.XmlEntity;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface ProfileDAO extends EntityDAO<UUID, Profile> {

    /**
     * @param member
     * @return
     */
    List<Profile> retrieveByMember(Member member);

    /**
     * @param xmlEntity
     * @return
     */
    Profile retrieveByXmlEntity(XmlEntity<?> xmlEntity);

    
}
