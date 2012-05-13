/**
 * 
 */
package org.brekka.pegasus.web.pages.networkgroup;

import java.util.UUID;

import org.apache.tapestry5.annotations.Property;
import org.brekka.pegasus.core.model.FirewallRule;
import org.brekka.pegasus.core.model.NetworkGroup;
import org.brekka.pegasus.core.services.FirewallService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class ModifyNetworkGroup {

    @Autowired
    private FirewallService firewallService;
    
    @Property
    private FirewallRule firewallRule;
    
    @Property
    private NetworkGroup networkGroup;
    
    void onActivate(String groupOrRuleIdStr) {
        UUID groupOrRuleId = UUID.fromString(groupOrRuleIdStr);
        networkGroup = firewallService.retrieveGroupById(groupOrRuleId);
        if (networkGroup == null) {
            firewallRule = firewallService.retrieveRuleById(groupOrRuleId);
            networkGroup = firewallRule.getNetworkGroup();
        }
    }
    
    String onPassivate() {
        return (networkGroup != null ? networkGroup.getId() : firewallRule.getId()).toString();
    }
    
    
    void init(NetworkGroup networkGroup) {
        this.networkGroup = networkGroup;
    }
}
