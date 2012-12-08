/**
 * 
 */
package org.brekka.pegasus.web.security;

import java.util.Collection;

import org.brekka.pegasus.core.model.Accessor;
import org.brekka.pegasus.core.services.impl.AccessorContextImpl;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class AnonymousAuthenticationToken extends AbstractAuthenticationToken implements Accessor {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -353817580361469260L;
    
    private final Object principal;
    
    private final AccessorContextImpl context = new AccessorContextImpl();
    
    
    public AnonymousAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.Authentication#getCredentials()
     */
    @Override
    public Object getCredentials() {
        return "";
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.Authentication#getPrincipal()
     */
    @Override
    public Object getPrincipal() {
        return principal;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.Accessor#getContext()
     */
    @Override
    public AccessorContextImpl getContext() {
        return context;
    }
}
