/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.brekka.pegasus.core.dao.FirewallDAO;
import org.brekka.pegasus.core.dao.FirewallRuleDAO;
import org.brekka.pegasus.core.dao.NetworkDAO;
import org.brekka.pegasus.core.dao.NetworkGroupDAO;
import org.brekka.pegasus.core.model.Firewall;
import org.brekka.pegasus.core.model.FirewallAction;
import org.brekka.pegasus.core.model.FirewallRule;
import org.brekka.pegasus.core.model.Network;
import org.brekka.pegasus.core.model.NetworkGroup;
import org.brekka.pegasus.core.model.NetworkGroupCategory;
import org.brekka.pegasus.core.services.FirewallService;
import org.brekka.xml.pegasus.v1.config.FirewallType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Service
@Transactional
public class FirewallServiceImpl implements FirewallService {

    @Autowired
    private FirewallDAO firewallDAO;
    @Autowired
    private FirewallRuleDAO firewallRuleDAO;
    @Autowired
    private NetworkDAO networkDAO;
    @Autowired
    private NetworkGroupDAO networkGroupDAO;
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.FirewallService#isAccessAllowed(java.lang.String, org.brekka.pegasus.core.model.Firewall)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public boolean isAccessAllowed(Firewall firewall, String ipAddress) {
        Firewall managedFirewall = firewallDAO.retrieveById(firewall.getId());
        List<FirewallRule> findApplicableRules = firewallRuleDAO.findApplicableRules(firewall, ipAddress);
        FirewallAction action = managedFirewall.getDefaultAction();
        if (!findApplicableRules.isEmpty()) {
            for (FirewallRule firewallRule : findApplicableRules) {
                action = firewallRule.getAction();
                break;
            }
        }
        return action == FirewallAction.ALLOW;
    }
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Firewall createFirewall(UUID owningEntityId, String name, FirewallAction firewallAction) {
        Firewall firewall = new Firewall();
        firewall.setOwningEntityId(owningEntityId);
        firewall.setName(name);
        firewall.setDefaultAction(firewallAction);
        firewallDAO.create(firewall);
        return firewall;
    }
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Network createNetwork(String cidrBlock, NetworkGroup inGroup) {
        Network network = new Network();
        network.setBlock(cidrBlock);
        network.setNetworkGroup(inGroup);
        networkDAO.create(network);
        return network;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.FirewallService#createNetworks(java.util.List, org.brekka.pegasus.core.model.NetworkGroup)
     */
    @Override
    public List<Network> createNetworks(List<String> cidrBlocks, NetworkGroup inGroup) {
        List<Network> networks = new ArrayList<>(cidrBlocks.size());
        for (String block : cidrBlocks) {
            Network network = createNetwork(block, inGroup);
            networks.add(network);
        }
        return networks;
    }
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public NetworkGroup createGroup(String name, NetworkGroupCategory networkGroupCategory) {
        NetworkGroup networkGroup = new NetworkGroup();
        networkGroup.setName(name);
        networkGroup.setNetworkGroupCategory(networkGroupCategory);
        networkGroupDAO.create(networkGroup);
        return networkGroup;
    }
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public FirewallRule createRule(String name, Firewall firewall, FirewallAction action, int priority) {
        NetworkGroup networkGroup = new NetworkGroup();
        networkGroup.setName(name);
        networkGroupDAO.create(networkGroup);
        
        FirewallRule rule = new FirewallRule();
        rule.setFirewall(firewall);
        rule.setAction(action);
        rule.setNetworkGroup(networkGroup);
        rule.setPriority(priority);
        firewallRuleDAO.create(rule);
        return rule;
    }
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public FirewallRule createRule(NetworkGroup networkGroup, Firewall firewall, FirewallAction action, int priority) {
        FirewallRule rule = new FirewallRule();
        rule.setFirewall(firewall);
        rule.setAction(action);
        rule.setNetworkGroup(networkGroup);
        rule.setPriority(priority);
        firewallRuleDAO.create(rule);
        return rule;
    }
    
    @Transactional(propagation=Propagation.REQUIRED)
    public Firewall retrieveConfiguredFirewall(FirewallType firewallConfig) {
        UUID systemId = UUID.fromString(firewallConfig.getID());
        Firewall firewall = firewallDAO.retrieveById(systemId);
        if (firewall == null) {
            firewall = createFirewall(systemId, firewallConfig.getName(), FirewallAction.DENY);
            FirewallRule rule = createRule("Default", firewall, FirewallAction.ALLOW, 1);
            NetworkGroup networkGroup = rule.getNetworkGroup();
            List<String> networkList = firewallConfig.getNetworkList();
            for (String block : networkList) {
                createNetwork(block, networkGroup);
            }
        }
        return firewall;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.FirewallService#retrieveFirewallsByOwner(java.util.UUID)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public List<Firewall> retrieveFirewallsByOwner(UUID owningEntityId) {
        return firewallDAO.retrieveByOwner(owningEntityId);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.FirewallService#retrieveNetworks(org.brekka.pegasus.core.model.NetworkGroup)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public List<Network> retrieveNetworks(NetworkGroup group) {
        return networkDAO.retrieveForGroup(group);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.FirewallService#retrieveRules(org.brekka.pegasus.core.model.Firewall)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public List<FirewallRule> retrieveRules(Firewall firewall) {
        return firewallRuleDAO.retrieveForFirewall(firewall);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.FirewallService#retrieveGroupById(java.util.UUID)
     */
    @Override
    public NetworkGroup retrieveGroupById(UUID groupId) {
        return networkGroupDAO.retrieveById(groupId);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.FirewallService#retrieveRuleById(java.util.UUID)
     */
    @Override
    public FirewallRule retrieveRuleById(UUID ruleId) {
        return firewallRuleDAO.retrieveById(ruleId);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.FirewallService#retrieveFirewallById(java.util.UUID)
     */
    @Override
    public Firewall retrieveFirewallById(UUID firewallId) {
        return firewallDAO.retrieveById(firewallId);
    }
}
