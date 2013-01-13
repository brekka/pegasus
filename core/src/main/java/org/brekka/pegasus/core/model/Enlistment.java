/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Defines the relationship between an associate and a division within an organization.
 * Essentially it stores the private key that can be used to unlock resources bound to this
 * division and its children.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@DiscriminatorValue("Enlistment")
public class Enlistment extends Connection<Associate, KeySafe<? extends Member>, Division<Organization>> {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 4972021130097729646L;
    
    public Division<Organization> getDivision() {
        return getTarget();
    }

    public void setDivision(Division<Organization> division) {
        setTarget(division);
    }

    public Associate getAssociate() {
        return getOwner();
    }

    public void setAssociate(Associate associate) {
        setOwner(associate);
    }

}
