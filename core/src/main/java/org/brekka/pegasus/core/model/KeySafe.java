/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

/**
 * Keeps keys safe.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`KeySafe`")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="`Type`", length=8,
    discriminatorType=DiscriminatorType.STRING
)
@DiscriminatorValue("Base")
public abstract class KeySafe extends LongevousEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -621238034501395611L;

}
