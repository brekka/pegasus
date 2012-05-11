/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

/**
 * An actor can be either a {@link Member} or an {@link Associate}. An employee is simply an association
 * of a member with an organization.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`Actor`")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="`Type`", length=8,
    discriminatorType=DiscriminatorType.STRING
)
@DiscriminatorValue("Actor")
public abstract class Actor extends LongevousEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 3647113396750700928L;

    /**
     * The current status of this actor.
     */
    @Enumerated(EnumType.STRING)
    @Column(name="`Status`", length=8, nullable=false)
    private ActorStatus status = ActorStatus.NEW;
    

    public final ActorStatus getStatus() {
        return status;
    }

    public final void setStatus(ActorStatus status) {
        this.status = status;
    }
}
