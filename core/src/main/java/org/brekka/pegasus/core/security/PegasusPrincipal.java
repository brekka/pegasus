/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.brekka.pegasus.core.security;

import java.io.Serializable;
import java.security.Principal;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.brekka.pegasus.core.model.AuthenticationToken;
import org.brekka.pegasus.core.model.MemberContext;


/**
 *
 */
public abstract class PegasusPrincipal implements Principal, Serializable {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 8387972887791453259L;

    private final UUID authenticationTokenId;

    private final String username;

    protected transient byte[] restoreSecret;

    protected transient MemberContext memberContext;



    protected PegasusPrincipal(final UUID authenticationTokenId, final String username) {
        this.authenticationTokenId = authenticationTokenId;
        this.username = username;
    }

    protected PegasusPrincipal(final AuthenticationToken authenticationToken) {
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

    public MemberContext getMemberContext() {
        return memberContext;
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

    public byte[] getRestoreSecret() {
        return restoreSecret;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("authTokenId", authenticationTokenId)
            .append("username", username)
            .toString();
    }
}
