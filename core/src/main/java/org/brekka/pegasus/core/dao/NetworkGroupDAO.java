/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.Firewall;
import org.brekka.pegasus.core.model.NetworkGroup;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface NetworkGroupDAO extends EntityDAO<UUID, NetworkGroup> {

    /**
     * @return
     */
    List<NetworkGroup> retrieveCategorized(Firewall excludeFrom);

}
