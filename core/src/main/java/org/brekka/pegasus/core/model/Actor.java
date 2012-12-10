/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.brekka.commons.persistence.model.LongevousEntity;
import org.brekka.pegasus.core.PegasusConstants;
import org.hibernate.annotations.Type;

/**
 * An actor can be either a {@link Member} or an {@link Associate}. An employee is simply an association
 * of a member with an organization.
 * 
 * TODO rename to Entity?
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`Actor`", schema=PegasusConstants.SCHEMA,
    uniqueConstraints={ 
        // Associate unique key
        @UniqueConstraint(columnNames = {"`MemberID`", "`OrganizationID`" }),
    }
)
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="`Type`", length=9,
    discriminatorType=DiscriminatorType.STRING
)
@DiscriminatorValue("Actor")
public abstract class Actor extends LongevousEntity<UUID> {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 3647113396750700928L;

    /**
     * Unique id
     */
    @Id
    @Type(type="pg-uuid")
    @Column(name="`ID`")
    private UUID id;

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

    /**
     * @return the id
     */
    public final UUID getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public final void setId(UUID id) {
        this.id = id;
    }
}
