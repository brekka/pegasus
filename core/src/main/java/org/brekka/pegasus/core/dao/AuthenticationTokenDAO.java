/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.AuthenticationToken;

/**
 * @author Andrew Taylor
 *
 */
public interface AuthenticationTokenDAO extends EntityDAO<UUID, AuthenticationToken> {


}
