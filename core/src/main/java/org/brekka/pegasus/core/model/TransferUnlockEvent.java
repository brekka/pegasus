/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.brekka.pegasus.core.PegasusConstants;

/**
 * When a transfer (of a bundle) is unlocked.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`TransferUnlockEvent`", schema=PegasusConstants.SCHEMA)
public class TransferUnlockEvent extends RemoteUserEvent {

    /**
     * Serial ID
     */
    private static final long serialVersionUID = 4307560513008867955L;

    /**
     * The transfer that was unlocked
     */
    @ManyToOne
    @JoinColumn(name="`TransferID`", nullable=false)
    private Transfer transfer;
    
    /**
     * Was the unlock successful
     */
    @Column(name="`Success`", nullable=false)
    private boolean success;

    public Transfer getTransfer() {
        return transfer;
    }

    public void setTransfer(Transfer transfer) {
        this.transfer = transfer;
    }

    /**
     * @return the success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @param success the success to set
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }
}
