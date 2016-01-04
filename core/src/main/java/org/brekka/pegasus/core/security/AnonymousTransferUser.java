/**
 *
 */
package org.brekka.pegasus.core.security;

import java.util.Arrays;
import java.util.Collection;

import org.brekka.pegasus.core.model.AccessorContextAware;
import org.brekka.pegasus.core.services.impl.AccessorContextImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class AnonymousTransferUser extends User implements AccessorContextAware {

    private static final Collection<GrantedAuthority> AUTHORITIES = Arrays.<GrantedAuthority>asList(PegasusAuthority.ANONYMOUS_TRANSFER);

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -353817580361469260L;

    private final AccessorContextImpl context = new AccessorContextImpl();

    /**
     * @param username
     * @param password
     * @param authorities
     */
    public AnonymousTransferUser(final String token) {
        super(token, token, AUTHORITIES);
    }

    @Override
    public AccessorContextImpl getAccessorContext() {
        return context;
    }

    public static AnonymousTransferUser getCurrent() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AnonymousTransferUser) {
            return (AnonymousTransferUser) principal;
        }
        return null;
    }

    /**
     * @param token
     * @return
     */
    public static boolean verifyToken(final String token) {
        AnonymousTransferUser current = getCurrent();
        if (current == null) {
            return false;
        }
        return current.getUsername().equals(token);
    }
}
