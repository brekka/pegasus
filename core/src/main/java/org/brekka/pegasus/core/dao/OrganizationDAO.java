/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Token;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface OrganizationDAO extends EntityDAO<UUID, Organization> {

    /**
     * @param token
     * @return
     */
    Organization retrieveByToken(Token token);

}
