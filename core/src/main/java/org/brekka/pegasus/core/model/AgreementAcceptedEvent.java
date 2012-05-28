/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Entity
@Table(name="`AgreementAcceptedEvent`")
public class AgreementAcceptedEvent extends RemoteUserEvent {
    /**
     * Serial UID
     */
    private static final long serialVersionUID = 5519875877999283084L;
    
    /**
     * The bundle that was created
     */
    @OneToOne
    @JoinColumn(name="`TransferID`", nullable=false, unique=true)
    private Transfer transfer;

    public Transfer getTransfer() {
        return transfer;
    }

    public void setTransfer(Transfer transfer) {
        this.transfer = transfer;
    }
}
