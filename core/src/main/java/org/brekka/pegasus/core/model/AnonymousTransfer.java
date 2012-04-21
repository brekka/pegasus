/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.brekka.commons.persistence.model.IdentifiableEntity;

/**
 * Enables an anonymous transfer
 * 
 * @author Andrew Taylor
 */
@Entity
@Table(name="\"AnonymousTransfer\"")
public class AnonymousTransfer extends IdentifiableEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -4542707737980018991L;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="Slug")
    private Slug slug;
    
    @OneToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="BundleID")
    private Bundle bundle;

    
    public Slug getSlug() {
        return slug;
    }

    public void setSlug(Slug slug) {
        this.slug = slug;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }
}
