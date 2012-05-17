/**
 * 
 */
package org.brekka.pegasus.web.support;

import java.util.UUID;

import org.apache.tapestry5.ValueEncoder;
import org.brekka.pegasus.core.model.NetworkGroup;
import org.brekka.pegasus.core.services.FirewallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Component
public class NetworkGroupEncoder implements ValueEncoder<NetworkGroup> {
    
    @Autowired
    private FirewallService firewallService;
    
    @Override
    public String toClient(NetworkGroup group) {
        return group.getId().toString();
    }

    @Override
    public NetworkGroup toValue(String clientValue) {
        return firewallService.retrieveGroupById(UUID.fromString(clientValue));
    }
}