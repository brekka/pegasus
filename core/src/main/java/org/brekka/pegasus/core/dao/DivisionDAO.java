/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.Division;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface DivisionDAO extends EntityDAO<UUID, Division<?>> {

    /**
     * @param organization
     * @param divisionSlug
     * @return
     */
    <Owner extends Actor> Division<Owner> retrieveBySlug(Owner owner, String divisionSlug);

    /**
     * @param organization
     * @return
     */
    <Owner extends Actor> Division<Owner> retrieveRootDivision(Owner owner);
}
