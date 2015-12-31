/**
 * Copyright (c) 2015 Digital Shadows Ltd.
 */

package org.brekka.pegasus.core.security;

import org.brekka.pegasus.core.model.AuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * Should be applied to the principal returned by {@link Authentication#getPrincipal()} to identify the Pegasus user currently active.
 */
public interface AuthenticationTokenAware {

    /**
     * Retrieve the authentication token associated with this principal.
     *
     * @return the token
     */
    AuthenticationToken getAuthenticationToken();
}
