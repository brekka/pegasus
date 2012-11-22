/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.OpenID;

/**
 * Open ID
 * 
 * @author Andrew Taylor
 */
public interface OpenIdDAO extends EntityDAO<UUID, OpenID> {

    OpenID retrieveByURI(String openIdUri);
}
