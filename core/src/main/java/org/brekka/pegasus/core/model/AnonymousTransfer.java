/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.brekka.commons.persistence.model.IdentifiableEntity;

/**
 * Enables an anonymous transfer
 * 
 * @author Andrew Taylor
 */
public class AnonymousTransfer extends IdentifiableEntity {

    @ManyToOne
    private Slug slug;
    
    @OneToOne
    private Bundle bundle;
}
