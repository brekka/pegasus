/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.brekka.commons.persistence.model.IdentifiableEntity;

/**
 * An inbox allows an individual on the internet to send a file to a {@link Member}. The public key of the vault will be
 * used to store the key of the uploaded file.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="\"Inbox\"")
public class Inbox extends IdentifiableEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 5531713690260375934L;

    /**
     * The token that identifies this inbox to the outside world.
     */
    @OneToOne
    @JoinColumn(name = "TokenID", nullable = false)
    private Token token;

    /**
     * The vault that will be used to store files added to this inbox. The user can change this vault at any time,
     * however the associated {@link Deposit} will become unavailable.
     */
    @ManyToOne
    @JoinColumn(name = "VaultID", nullable = false)
    private Vault vault;
    
    /**
     * The owner of this inbox
     */
    @ManyToOne
    @JoinColumn(name="OwnerID", nullable = false)
    private Member owner;

    
    public Vault getVault() {
        return vault;
    }

    public void setVault(Vault vault) {
        this.vault = vault;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public Member getOwner() {
        return owner;
    }

    public void setOwner(Member owner) {
        this.owner = owner;
    }
}
