/**
 * 
 */
package org.brekka.pegasus.core.security;

import org.springframework.security.core.GrantedAuthority;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public enum PegasusAuthority implements GrantedAuthority {
    
    USER,
    
    ADMIN,
    
    ANONYMOUS_TRANSFER,
    
    MEMBER_SIGNUP,
    
    ANONYMOUS,
    
    ;

    private final String role;
    
    /**
     * 
     */
    private PegasusAuthority() {
        this.role = "ROLE_" + name();
    }
    
    /* (non-Javadoc)
     * @see org.springframework.security.core.GrantedAuthority#getAuthority()
     */
    @Override
    public String getAuthority() {
        return role;
    }
    
    public String toString() {
        return this.role;
    }

}
