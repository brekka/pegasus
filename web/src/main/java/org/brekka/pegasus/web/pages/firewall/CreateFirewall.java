/**
 * 
 */
package org.brekka.pegasus.web.pages.firewall;

import java.util.UUID;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.brekka.pegasus.core.model.Firewall;
import org.brekka.pegasus.core.model.FirewallAction;
import org.brekka.pegasus.core.services.FirewallService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class CreateFirewall {
    @InjectPage
    private ModifyFirewall modifyFirewallPage;
    
    @Autowired
    private FirewallService firewallService;
    
    
    @Property
    private String name;
    
    @Property
    private FirewallAction defaultAction;
    
    @Property
    private UUID owningEntityId;
    
    void onActivate(String owningEntityIdStr) {
        this.owningEntityId = UUID.fromString(owningEntityIdStr);
    }
    
    String onPassivate() {
        return owningEntityId.toString();
    }
    
    Object onSuccess() {
        Firewall firewall = firewallService.createFirewall(owningEntityId, name, defaultAction);
        modifyFirewallPage.init(firewall);
        return modifyFirewallPage;
    }
}
