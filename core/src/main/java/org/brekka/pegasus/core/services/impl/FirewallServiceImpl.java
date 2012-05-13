/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import org.brekka.pegasus.core.model.Firewall;
import org.brekka.pegasus.core.services.FirewallService;
import org.springframework.stereotype.Service;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Service
public class FirewallServiceImpl implements FirewallService {

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.FirewallService#isAccessAllowed(java.lang.String, org.brekka.pegasus.core.model.Firewall)
     */
    @Override
    public boolean isAccessAllowed(String ipAddress, Firewall firewall) {
        // TODO Auto-generated method stub
        return false;
    }
    
}
