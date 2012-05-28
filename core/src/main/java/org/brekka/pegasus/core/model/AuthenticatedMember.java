/**
 * 
 */
package org.brekka.pegasus.core.model;

import org.brekka.xml.pegasus.v1.model.ProfileType;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author Andrew Taylor
 *
 */
public interface AuthenticatedMember {

    Actor getActiveActor();
    
    Member getMember();
    
    Vault getActiveVault();
    
    ProfileType getProfile();
    
    boolean hasAccess(GrantedAuthority grantedAuthority);
    
}
