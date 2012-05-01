/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * When a bundle is unlocked.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="\"BundleUnlockEvent\"")
public class BundleUnlockEvent extends RemoteUserEvent {

    /**
     * Serial ID
     */
    private static final long serialVersionUID = 4307560513008867955L;

    @ManyToOne
    @JoinColumn(name="BundleID", nullable=false)
    private Bundle bundle;
    
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date agreementAccepted;

    
    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public Date getAgreementAccepted() {
        return agreementAccepted;
    }

    public void setAgreementAccepted(Date agreementAccepted) {
        this.agreementAccepted = agreementAccepted;
    }
}
