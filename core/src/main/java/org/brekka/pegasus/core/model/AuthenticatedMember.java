/**
 * 
 */
package org.brekka.pegasus.core.model;

import org.brekka.xml.pegasus.v2.model.ProfileType;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author Andrew Taylor
 *
 */
public interface AuthenticatedMember<T extends Member> extends Accessor {

    Actor getActiveActor();
    
    T getMember();
    
    Vault getActiveVault();
    
    ProfileType getProfile();
    
    boolean hasAccess(GrantedAuthority grantedAuthority);
}
