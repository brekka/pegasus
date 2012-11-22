/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * A member of the site, can be either a {@link Person} or a {@link Robot}.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@DiscriminatorValue("Member")
public abstract class Member extends Actor {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -6815079717237157048L;


    /**
     * Authentication token that identifies this member
     */
    @OneToOne
    @JoinColumn(name="`AuthenticationTokenID`")
    private AuthenticationToken authenticationToken;

    /**
     * @return the authenticationToken
     */
    public AuthenticationToken getAuthenticationToken() {
        return authenticationToken;
    }

    /**
     * @param authenticationToken the authenticationToken to set
     */
    public void setAuthenticationToken(AuthenticationToken authenticationToken) {
        this.authenticationToken = authenticationToken;
    }
}
