/**
 * 
 */
package org.brekka.pegasus.web.pages.firewall;

import java.util.UUID;

import org.apache.tapestry5.annotations.Property;
import org.brekka.pegasus.core.model.Firewall;
import org.brekka.pegasus.core.services.FirewallService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Manipulate the firewall - add/remove rules.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class ModifyFirewall {
    @Autowired
    private FirewallService firewallService;
    
    @Property
    private Firewall firewall;
    
    void onActivate(String firewallIdStr) {
        UUID firewallId = UUID.fromString(firewallIdStr);
        firewall = firewallService.retrieveFirewallById(firewallId);
    }
    
    String onPassivate() {
        return firewall.getId().toString();
    }
    
    void init(Firewall firewall) {
        this.firewall = firewall;
    }
    
}
