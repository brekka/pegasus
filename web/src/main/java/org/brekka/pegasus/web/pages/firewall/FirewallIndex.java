/**
 * 
 */
package org.brekka.pegasus.web.pages.firewall;

import java.util.UUID;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.model.Firewall;
import org.brekka.pegasus.core.services.FirewallService;
import org.brekka.pegasus.web.support.Configuration;
import org.brekka.xml.pegasus.v2.config.FirewallType;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class FirewallIndex {

    @Inject
    private FirewallService firewallService;
    
    @Inject
    private Configuration configuration;
    
    
    public UUID getAnonymousTransferKey() {
        FirewallType firewallType = configuration.getRoot().getAnonymousTransfer().getFirewall();
        Firewall firewall = firewallService.retrieveConfiguredFirewall(firewallType);
        return firewall.getId();
    }
    
    public UUID getMemberSetupKey() {
        FirewallType firewallType = configuration.getRoot().getMemberSignup().getFirewall();
        Firewall firewall = firewallService.retrieveConfiguredFirewall(firewallType);
        return firewall.getId();
    }
}
