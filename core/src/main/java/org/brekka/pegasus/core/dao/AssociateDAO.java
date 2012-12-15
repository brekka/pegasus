/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Vault;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface AssociateDAO extends EntityDAO<UUID, Associate> {

    /**
     * @param keySafe
     * @return
     */
    List<Associate> retrieveAssociates(Member member);

    /**
     * @param organization
     * @param member
     * @return
     */
    Associate retrieveByOrgAndMember(Organization organization, Member member);

}
