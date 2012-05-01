/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.brekka.commons.persistence.model.IdentifiableEntity;

/**
 * Represents a file which has been deposited with a member via their public key. Essentially it links
 * together a {@link Bundle}, {@link Inbox} and {@link Vault}.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="\"Deposit\"")
public class Deposit extends IdentifiableEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -3907319818384864026L;

    /**
     * The bundle represented by this deposit
     */
    @OneToOne
    @JoinColumn(name="BundleID", unique=true, updatable=false, nullable=false)
    private Bundle bundle;
    
    /**
     * Identifies the origin of the bundle.
     */
    @ManyToOne
    @JoinColumn(name="InboxID", updatable=false, nullable=false)
    private Inbox inbox;
    
    /**
     * The vault that will contain the encryption key for the bundle
     */
    @ManyToOne
    @JoinColumn(name="VaultID", updatable=false, nullable=false)
    private Vault vault;
    
    /**
     * When created
     */
    @Column(name="Created", updatable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public Inbox getInbox() {
        return inbox;
    }

    public void setInbox(Inbox inbox) {
        this.inbox = inbox;
    }

    public Vault getVault() {
        return vault;
    }

    public void setVault(Vault vault) {
        this.vault = vault;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
