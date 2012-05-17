/**
 * 
 */
package org.brekka.pegasus.web.pages.networkgroup;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.model.FirewallRule;
import org.brekka.pegasus.core.model.Network;
import org.brekka.pegasus.core.model.NetworkGroup;
import org.brekka.pegasus.core.services.FirewallService;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class ModifyNetworkGroup {
    
    private static final Pattern CIDR_PATTERN = Pattern.compile(
            "((?:\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}/[1-3]?[0-9])" + // IPv4
            "|(?:[a-f0-9\\:]+/\\d+))"); // IPv6

    @InjectComponent
    private Zone networkZone;
    
    @Inject
    private FirewallService firewallService;
    
    @Property
    private FirewallRule firewallRule;
    
    @Property
    private NetworkGroup networkGroup;
    
    @Property
    private Network loopNetwork;
    
    @Property
    private String blocks;
    
    void onActivate(String groupOrRuleIdStr) {
        UUID groupOrRuleId = UUID.fromString(groupOrRuleIdStr);
        networkGroup = firewallService.retrieveGroupById(groupOrRuleId);
        if (networkGroup == null) {
            firewallRule = firewallService.retrieveRuleById(groupOrRuleId);
            networkGroup = firewallRule.getNetworkGroup();
        }
    }
    
    String onPassivate() {
        return (firewallRule == null ? networkGroup.getId() : firewallRule.getId()).toString();
    }
    
    Object onSuccess() {
        Matcher m = CIDR_PATTERN.matcher(blocks);
        List<String> blockList = new ArrayList<>();
        while (m.find()) {
            blockList.add(m.group(1));
        }
        
        firewallService.createNetworks(blockList, networkGroup);
        
        return networkZone.getBody();
    }
    
    Object onActionFromRemove(String networkId) {
        UUID id = UUID.fromString(networkId);
        firewallService.deleteNetwork(id);
        return networkZone.getBody();
    }
    
    
    void init(NetworkGroup networkGroup) {
        this.networkGroup = networkGroup;
    }
    
    public List<Network> getNetworks() {
        return firewallService.retrieveNetworks(networkGroup);
    }

    /**
     * @param rule
     */
    public void init(FirewallRule rule) {
        this.networkGroup = rule.getNetworkGroup();
        this.firewallRule = rule;
    }
}
