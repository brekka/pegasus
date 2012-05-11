/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.DomainName;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface DomainNameDAO extends EntityDAO<UUID, DomainName> {

    /**
     * @param hash
     */
    DomainName retrieveByHash(byte[] hash);

}
