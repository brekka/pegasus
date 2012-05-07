/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * An autonomous member of the system that can act on another users behalf. 
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@DiscriminatorValue("Robot")
public class Robot extends Member {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 1656293508329918826L;

}
