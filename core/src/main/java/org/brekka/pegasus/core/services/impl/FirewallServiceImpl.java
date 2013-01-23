/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.brekka.xml.pegasus.v2.config.FirewallType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Firewall
 * 
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
    @Transactional(readOnly=true)
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
    @Transactional()
    public Firewall createFirewall(UUID owningEntityId, String name, FirewallAction firewallAction) {
        Firewall firewall = new Firewall();
        firewall.setOwningEntityId(owningEntityId);
        firewall.setName(name);
        firewall.setDefaultAction(firewallAction);
        firewallDAO.create(firewall);
        return firewall;
    }
    
    @Override
    @Transactional()
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
    @Transactional()
    public NetworkGroup createGroup(String name, NetworkGroupCategory networkGroupCategory) {
        NetworkGroup networkGroup = new NetworkGroup();
        networkGroup.setName(name);
        networkGroup.setNetworkGroupCategory(networkGroupCategory);
        networkGroupDAO.create(networkGroup);
        return networkGroup;
    }
    
    @Override
    @Transactional()
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
    @Transactional()
    public FirewallRule createRule(NetworkGroup networkGroup, Firewall firewall, FirewallAction action, int priority) {
        FirewallRule rule = new FirewallRule();
        rule.setFirewall(firewall);
        rule.setAction(action);
        rule.setNetworkGroup(networkGroup);
        rule.setPriority(priority);
        firewallRuleDAO.create(rule);
        return rule;
    }
    
    @Transactional(readOnly=true)
    public Firewall retrieveConfiguredFirewall(FirewallType firewallConfig) {
        UUID systemId = UUID.fromString(firewallConfig.getID());
        List<Firewall> firewallList = firewallDAO.retrieveByOwningEntity(systemId);
        Firewall firewall;
        if (firewallList.isEmpty()) {
            firewall = createFirewall(systemId, firewallConfig.getName(), FirewallAction.DENY);
            FirewallRule rule = createRule("Default", firewall, FirewallAction.ALLOW, 1);
            NetworkGroup networkGroup = rule.getNetworkGroup();
            List<String> networkList = firewallConfig.getNetworkList();
            for (String block : networkList) {
                createNetwork(block, networkGroup);
            }
        } else {
            firewall = firewallList.get(0);
        }
        return firewall;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.FirewallService#updateFirewall(java.util.UUID, java.lang.String, org.brekka.pegasus.core.model.FirewallAction)
     */
    @Override
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public Firewall updateFirewall(UUID id, String firewallName, FirewallAction firewallAction) {
        Firewall firewall = firewallDAO.retrieveById(id);
        firewall.setName(firewallName);
        firewall.setDefaultAction(firewallAction);
        firewallDAO.update(firewall);
        return firewall;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.FirewallService#deleteNetwork(java.util.UUID)
     */
    @Override
    @Transactional()
    public void deleteNetwork(UUID networkId) {
        networkDAO.delete(networkId);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.FirewallService#deleteRule(java.util.UUID)
     */
    @Override
    @Transactional()
    public void deleteRule(UUID id) {
        FirewallRule rule = firewallRuleDAO.retrieveById(id);
        NetworkGroup networkGroup = rule.getNetworkGroup();
        firewallRuleDAO.delete(id);
        if (networkGroup.getNetworkGroupCategory() == null) {
            networkGroupDAO.delete(networkGroup.getId());
        }
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.FirewallService#retrieveCategorizedGroups()
     */
    @Override
    @Transactional(readOnly=true)
    public List<NetworkGroup> retrieveCategorizedGroups(Firewall excludeFrom) {
        return networkGroupDAO.retrieveCategorized(excludeFrom);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.FirewallService#retrieveFirewallsByOwner(java.util.UUID)
     */
    @Override
    @Transactional(readOnly=true)
    public List<Firewall> retrieveFirewallsByOwner(UUID owningEntityId) {
        return firewallDAO.retrieveByOwningEntity(owningEntityId);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.FirewallService#retrieveNetworks(org.brekka.pegasus.core.model.NetworkGroup)
     */
    @Override
    @Transactional(readOnly=true)
    public List<Network> retrieveNetworks(NetworkGroup group) {
        return networkDAO.retrieveForGroup(group);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.FirewallService#retrieveRules(org.brekka.pegasus.core.model.Firewall)
     */
    @Override
    @Transactional(readOnly=true)
    public List<FirewallRule> retrieveRules(Firewall firewall) {
        return firewallRuleDAO.retrieveForFirewall(firewall);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.FirewallService#retrieveGroupById(java.util.UUID)
     */
    @Override
    @Transactional(readOnly=true)
    public NetworkGroup retrieveGroupById(UUID groupId) {
        return networkGroupDAO.retrieveById(groupId);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.FirewallService#retrieveRuleById(java.util.UUID)
     */
    @Override
    @Transactional(readOnly=true)
    public FirewallRule retrieveRuleById(UUID ruleId) {
        return firewallRuleDAO.retrieveById(ruleId);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.FirewallService#retrieveFirewallById(java.util.UUID)
     */
    @Override
    @Transactional(readOnly=true)
    public Firewall retrieveFirewallById(UUID firewallId) {
        return firewallDAO.retrieveById(firewallId);
    }
}
