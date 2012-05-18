/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.KeySafe;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface AssociateDAO extends EntityDAO<UUID, Associate> {

    /**
     * @param keySafe
     * @return
     */
    List<Associate> retrieveAssociatesInKeySafe(KeySafe keySafe);

}
