/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

/**
 * Represents a file which has been deposited with a member via their public key. Essentially it links
 * together a {@link Bundle}, {@link Inbox} and {@link Vault}.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`Deposit`")
public class Deposit extends SnapshotEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -3907319818384864026L;

    /**
     * The bundle represented by this deposit
     */
    @OneToOne
    @JoinColumn(name="`BundleID`", unique=true, updatable=false, nullable=false)
    private Bundle bundle;
    
    /**
     * Id of the crypted data that contains the key used to encrypt this file's parts.
     */
    @Type(type="pg-uuid")
    @Column(name="`CryptedDataID`")
    private UUID cryptedDataId;
    
    /**
     * Identifies the origin of the bundle.
     */
    @ManyToOne
    @JoinColumn(name="`InboxID`", updatable=false, nullable=false)
    private Inbox inbox;
    
    /**
     * The key safe that will contain the encryption key for the bundle
     */
    @ManyToOne
    @JoinColumn(name="`KeySafeID`", updatable=false, nullable=false)
    private KeySafe keySafe;
    
    
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

    public KeySafe getKeySafe() {
        return keySafe;
    }

    public void setKeySafe(KeySafe keySafe) {
        this.keySafe = keySafe;
    }
    
    public UUID getCryptedDataId() {
        return cryptedDataId;
    }

    public void setCryptedDataId(UUID cryptedDataId) {
        this.cryptedDataId = cryptedDataId;
    }
}
