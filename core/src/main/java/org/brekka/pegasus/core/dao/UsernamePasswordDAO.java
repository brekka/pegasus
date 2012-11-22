/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.UsernamePassword;

/**
 * Username/password based authentication token DAO 
 * 
 * @author Andrew Taylor
 */
public interface UsernamePasswordDAO extends EntityDAO<UUID, UsernamePassword> {

    UsernamePassword retrieveByUsername(String username);
}
