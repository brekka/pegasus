/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * When a bundle is unlocked.
 * 
 * @author Andrew Taylor
 */
@Entity
@Table(name="\"BundleCreatedEvent\"")
public class BundleCreatedEvent extends RemoteUserEvent {

    /**
     * Serial ID
     */
    private static final long serialVersionUID = 4307560513008867955L;

    @ManyToOne
    @JoinColumn(name="BundleID", nullable=false)
    private Bundle bundle;
    
    
    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }
}
