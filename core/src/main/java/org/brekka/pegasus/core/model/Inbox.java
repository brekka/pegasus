/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

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
    
    /**
     * Text to be displayed to the person when depositing a file.
     */
    @Column(name="Introduction", length=2000)
    private String introduction;
    
    /**
     * Records the current status of this inbox
     */
    @Column(name="Status", length=8, nullable=false)
    @Enumerated(EnumType.STRING)
    private InboxStatus status;
    
    /**
     * Name will be stored separately
     */
    @Transient
    private transient String name;

    
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

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InboxStatus getStatus() {
        return status;
    }

    public void setStatus(InboxStatus status) {
        this.status = status;
    }
}
