/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.Firewall;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface FirewallDAO extends EntityDAO<UUID, Firewall>  {

    boolean isAccessAllowed(String ipAddress, Firewall firewall);
}
