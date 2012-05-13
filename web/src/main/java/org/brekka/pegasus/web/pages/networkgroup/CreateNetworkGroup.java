/**
 * 
 */
package org.brekka.pegasus.web.pages.networkgroup;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.brekka.pegasus.core.model.NetworkGroup;
import org.brekka.pegasus.core.model.NetworkGroupCategory;
import org.brekka.pegasus.core.services.FirewallService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Creates a categorized network group that can be looked up and referenced by its category.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class CreateNetworkGroup {
    @InjectPage
    private ModifyNetworkGroup modifyNetworkGroupPage;
    
    @Autowired
    private FirewallService firewallService;
    
    @Property
    private String name;
    
    @Property
    private NetworkGroupCategory category;
    
    
    Object onSuccess() {
        NetworkGroup networkGroup = firewallService.createGroup(name, category);
        modifyNetworkGroupPage.init(networkGroup);
        return modifyNetworkGroupPage;
    }
}
