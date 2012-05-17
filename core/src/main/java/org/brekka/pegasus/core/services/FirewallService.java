/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.List;
import java.util.UUID;

import org.brekka.pegasus.core.model.Firewall;
import org.brekka.pegasus.core.model.FirewallAction;
import org.brekka.pegasus.core.model.FirewallRule;
import org.brekka.pegasus.core.model.Network;
import org.brekka.pegasus.core.model.NetworkGroup;
import org.brekka.pegasus.core.model.NetworkGroupCategory;
import org.brekka.xml.pegasus.v1.config.FirewallType;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface FirewallService {

    boolean isAccessAllowed(Firewall firewall, String ipAddress);
    
    FirewallRule createRule(NetworkGroup networkGroup, Firewall firewall, FirewallAction action, int priority);

    FirewallRule createRule(String name, Firewall firewall, FirewallAction action, int priority);

    NetworkGroup createGroup(String name, NetworkGroupCategory networkGroupCategory);

    Network createNetwork(String cidrBlock, NetworkGroup inGroup);
    
    List<Network> createNetworks(List<String> cidrBlocks, NetworkGroup inGroup);

    Firewall createFirewall(UUID owningEntityId, String name, FirewallAction firewallAction);
    
    Firewall retrieveConfiguredFirewall(FirewallType firewallConfig);
    
    List<Firewall> retrieveFirewallsByOwner(UUID owningEntityId);
    
    List<FirewallRule> retrieveRules(Firewall firewall);
    
    List<Network> retrieveNetworks(NetworkGroup group);
    
    NetworkGroup retrieveGroupById(UUID groupId);

    FirewallRule retrieveRuleById(UUID ruleId);

    /**
     * @param firewallId
     * @return
     */
    Firewall retrieveFirewallById(UUID firewallId);

    /**
     * @return
     */
    List<NetworkGroup> retrieveCategorizedGroups(Firewall excludeFrom);

    /**
     * @param id
     */
    void deleteNetwork(UUID networkId);

    /**
     * @param id
     */
    void deleteRule(UUID id);

    /**
     * @param id
     * @param firewallName
     * @param firewallAction
     * @return
     */
    Firewall updateFirewall(UUID id, String firewallName, FirewallAction firewallAction);
}
