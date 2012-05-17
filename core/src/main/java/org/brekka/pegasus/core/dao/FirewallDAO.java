/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.Firewall;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface FirewallDAO extends EntityDAO<UUID, Firewall>  {

    /**
     * @param owningEntityId
     * @return
     */
    List<Firewall> retrieveByOwningEntity(UUID owningEntityId);


}
