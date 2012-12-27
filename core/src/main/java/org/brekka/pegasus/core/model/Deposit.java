/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Represents a file which has been deposited with a member via their public key. Essentially it links
 * together an {@link Inbox} and {@link KeySafe}.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@DiscriminatorValue("Deposit")
public class Deposit extends Transfer implements KeySafeAware {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -3907319818384864026L;
    
    /**
     * Identifies the origin of the bundle.
     */
    @ManyToOne
    @JoinColumn(name="`InboxID`", updatable=false)
    private Inbox inbox;
    
    /**
     * The key safe that will contain the encryption key for the bundle
     */
    @ManyToOne
    @JoinColumn(name="`KeySafeID`", updatable=false)
    private KeySafe<?> keySafe;
    
    
    public Inbox getInbox() {
        return inbox;
    }

    public void setInbox(Inbox inbox) {
        this.inbox = inbox;
    }

    @Override
    public KeySafe<?> getKeySafe() {
        return keySafe;
    }

    public void setKeySafe(KeySafe<?> keySafe) {
        this.keySafe = keySafe;
    }
}
