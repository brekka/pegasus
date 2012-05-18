/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.Organization;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface DivisionDAO extends EntityDAO<UUID, Division> {

    /**
     * @param organization
     * @param divisionSlug
     * @return
     */
    Division retrieveBySlug(Organization organization, String divisionSlug);
}
