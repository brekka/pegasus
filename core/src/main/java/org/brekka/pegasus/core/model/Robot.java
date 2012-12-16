/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SecondaryTable;

import org.brekka.pegasus.core.PegasusConstants;

/**
 * An autonomous member of the system that can act on another users behalf. 
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@DiscriminatorValue("Robot")
@SecondaryTable(name="`Robot`", schema=PegasusConstants.SCHEMA)
public class Robot extends Member {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 1656293508329918826L;

    
    /**
     * The owner o
     */
    @ManyToOne
    @JoinColumn(name="`OwnerID`", table="`Robot`")
    private Actor owner;
}
