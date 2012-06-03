/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.brekka.pegasus.core.PegasusConstants;

/**
 * When a bundle is created.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`TransferCreatedEvent`", schema=PegasusConstants.SCHEMA)
public class TransferCreatedEvent extends RemoteUserEvent {

    /**
     * Serial ID
     */
    private static final long serialVersionUID = 4307560513008867955L;

    /**
     * The transfer that was created
     */
    @ManyToOne
    @JoinColumn(name="`TransferID`", nullable=false)
    private Transfer transfer;

    public Transfer getTransfer() {
        return transfer;
    }

    public void setTransfer(Transfer transfer) {
        this.transfer = transfer;
    }
    
}
