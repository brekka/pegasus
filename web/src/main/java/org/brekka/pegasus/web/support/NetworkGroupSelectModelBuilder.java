/**
 * 
 */
package org.brekka.pegasus.web.support;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.brekka.pegasus.core.model.Firewall;
import org.brekka.pegasus.core.model.NetworkGroup;
import org.brekka.pegasus.core.services.FirewallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Component
public class NetworkGroupSelectModelBuilder {
    
    @Autowired
    private FirewallService firewallService;
    
    public SelectModel getCategorized(Firewall excludeFrom) {
        List<NetworkGroup> groupList = firewallService.retrieveCategorizedGroups(excludeFrom);
        List<OptionModel> options = new ArrayList<>(groupList.size());
        for (NetworkGroup networkGroup : groupList) {
            options.add(new OptionModelImpl(networkGroup.getName(), networkGroup));
        }
        return new SelectModelImpl(null, options);
    }
}