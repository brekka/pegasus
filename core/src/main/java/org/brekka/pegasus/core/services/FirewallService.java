/**
 * 
 */
package org.brekka.pegasus.core.services;

import org.brekka.pegasus.core.model.Firewall;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface FirewallService {

    boolean isAccessAllowed(String ipAddress, Firewall firewall);
}
