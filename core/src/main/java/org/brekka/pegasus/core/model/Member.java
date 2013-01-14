/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SecondaryTable;

import org.brekka.pegasus.core.PegasusConstants;

/**
 * A member of the site, can be either a {@link Person} or a {@link Robot}.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@DiscriminatorValue("Member")
@SecondaryTable(name="`Member`", schema=PegasusConstants.SCHEMA)
public abstract class Member extends Actor {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -6815079717237157048L;


    /**
     * Authentication token that identifies this member
     */
    @OneToOne
    @JoinColumn(name="`AuthenticationTokenID`", table="`Member`", nullable=false)
    private AuthenticationToken authenticationToken;
    
    /**
     * The default vault for this member.
     */
    @OneToOne
    @JoinColumn(name="`DefaultVaultID`", table="`Member`")
    private Vault defaultVault;
    
    /**
     * The primary keySafe of this member. Depending on the policy applied to the user, this may be just a reference to the defaultVault,
     * some other vault or some other keySafe such as a division. When allocating a resource to a user, this keySafe should be used in
     * preference over the defaultVault unless the resource should be explicitly tied to the passworded keypair.
     */
    @OneToOne
    @JoinColumn(name = "`PrimaryKeySafeID`", table="`Member`")
    private KeySafe<? extends Member> primaryKeySafe;

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

    public final Vault getDefaultVault() {
        return defaultVault;
    }

    public final void setDefaultVault(Vault defaultVault) {
        this.defaultVault = defaultVault;
    }
    

    /**
     * @return the primaryKeySafe
     */
    public final KeySafe<? extends Member> getPrimaryKeySafe() {
        return primaryKeySafe;
    }

    /**
     * @param primaryKeySafe
     *            the primaryKeySafe to set
     */
    public final void setPrimaryKeySafe(KeySafe<? extends Member> primaryKeySafe) {
        this.primaryKeySafe = primaryKeySafe;
    }
}
