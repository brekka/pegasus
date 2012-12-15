/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.Enlistment;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Organization;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface EnlistmentDAO extends EntityDAO<UUID, Enlistment> {

    /**
     * @param organization
     * @param associate
     * @return
     */
    List<Enlistment> retrieveForAssociate(Associate associate);

    /**
     * @param target
     * @param member
     * @return
     */
    Enlistment retrieveEnlistmentByTarget(Division<Organization> target, Associate associate);

}
