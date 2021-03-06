/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.Token;

/**
 * @author Andrew Taylor
 *
 */
public interface TokenDAO extends EntityDAO<UUID, Token> {

    Token retrieveByPath(String path);
}
