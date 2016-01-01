/**
 * Copyright (c) 2016 Digital Shadows Ltd.
 */

package org.brekka.pegasus.core.security;

import java.io.Serializable;
import java.security.Principal;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.brekka.pegasus.core.model.AuthenticationToken;


/**
 *
 */
public final class PegasusPrincipal implements Principal, Serializable {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 8387972887791453259L;

    private final UUID authenticationTokenId;

    private final String username;


    public PegasusPrincipal(final UUID authenticationTokenId, final String username) {
        this.authenticationTokenId = authenticationTokenId;
        this.username = username;
    }

    public PegasusPrincipal(final AuthenticationToken authenticationToken) {
        this(authenticationToken.getId(), authenticationToken.getUsername());
    }

    public UUID getAuthenticationTokenId() {
        return authenticationTokenId;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) {
          return false;
        }
        PegasusPrincipal rhs = (PegasusPrincipal) obj;
        return authenticationTokenId.equals(rhs.authenticationTokenId);
    }

    @Override
    public int hashCode() {
        return authenticationTokenId.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("authTokenId", authenticationTokenId)
            .append("username", username)
            .toString();
    }
}
