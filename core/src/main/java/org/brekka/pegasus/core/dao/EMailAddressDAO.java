/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.EMailAddress;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface EMailAddressDAO extends EntityDAO<UUID, EMailAddress> {

    /**
     * @param hash
     * @return
     */
    EMailAddress retrieveByHash(byte[] hash);

}
