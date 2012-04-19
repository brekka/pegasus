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
 * @author Andrew Taylor
 */
@Entity
@Table(name="\"BundleUnlockEvent\"")
public class BundleUnlockEvent extends RemoteUserEvent {

    @ManyToOne
    @JoinColumn(name="BundleID")
    private Bundle bundle;
    
    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date agreementAccepted;
}
