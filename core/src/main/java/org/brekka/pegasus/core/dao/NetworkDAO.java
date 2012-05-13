/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.Network;
import org.brekka.pegasus.core.model.NetworkGroup;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface NetworkDAO extends EntityDAO<UUID, Network> {

    /**
     * @param group
     * @return
     */
    List<Network> retrieveForGroup(NetworkGroup group);

}
