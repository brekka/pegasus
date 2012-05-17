/**
 * 
 */
package org.brekka.pegasus.web.pages.networkgroup;

import java.util.List;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.model.NetworkGroup;
import org.brekka.pegasus.core.services.FirewallService;

/**
 * List of network groups that have categorization. Non-categorized will not be listed here.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class NetworkGroupIndex {

    @Inject
    private FirewallService firewallService;
    
    @Property
    private NetworkGroup loopGroup;
    
    public List<NetworkGroup> getGroups() {
        return firewallService.retrieveCategorizedGroups(null);
    }
    
}
