/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * A bundle of files added to a drop target
 * 
 * @author Andrew Taylor
 *
 */
//@Entity
//@Table(name="\"DropTargetBundle\"")
public class DropTargetBundle {

    @ManyToOne
    private DropTarget dropTarget;
    
    @OneToOne
    private Bundle bundle;
}
