/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.Firewall;
import org.brekka.pegasus.core.model.FirewallRule;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface FirewallRuleDAO extends EntityDAO<UUID, FirewallRule> {

    List<FirewallRule> findApplicableRules(Firewall firewall, String ipAddress);

    /**
     * @param firewall
     * @return
     */
    List<FirewallRule> retrieveForFirewall(Firewall firewall);
}
