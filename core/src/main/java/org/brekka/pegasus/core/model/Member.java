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
 * @author Andrew Taylor
 */
@Entity
@DiscriminatorValue("Member")
public abstract class Member extends Actor {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -6815079717237157048L;

    
    @OneToOne
    @JoinColumn(name="`DefaultVaultID`")
    private Vault defaultVault;

    public Vault getDefaultVault() {
        return defaultVault;
    }

    public void setDefaultVault(Vault defaultVault) {
        this.defaultVault = defaultVault;
    }
}
