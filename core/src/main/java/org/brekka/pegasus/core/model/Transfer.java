/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * A transfer of a bundle to another party.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@DiscriminatorValue("Transfer")
public abstract class Transfer extends AllocatedBundle {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -6144176815399227699L;

}
