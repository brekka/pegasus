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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.brekka.commons.persistence.model.LongevousEntity;
import org.brekka.pegasus.core.PegasusConstants;
import org.hibernate.annotations.Type;

/**
 * Keeps keys safe.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`KeySafe`", schema=PegasusConstants.SCHEMA,
    uniqueConstraints={ 
        // Division slugs must be unique within the the organization. 
        @UniqueConstraint(columnNames = {"`OrganizationID`", "`Slug`"}),
        // Vault slugs must be unique to the owner. 
        @UniqueConstraint(columnNames = {"`OwnerID`", "`Slug`"}) 
    }
)
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="`Type`", length=8,
    discriminatorType=DiscriminatorType.STRING
)
@DiscriminatorValue("Base")
public abstract class KeySafe<Owner extends Actor> extends LongevousEntity<UUID> {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -621238034501395611L;

    /**
     * Unique id
     */
    @Id
    @Type(type="pg-uuid")
    @Column(name="`ID`")
    private UUID id;
    
    /**
     * Records the current status of this vault
     */
    @Column(name="`Status`", length=8, nullable=false)
    @Enumerated(EnumType.STRING)
    private KeySafeStatus status = KeySafeStatus.ACTIVE;
    
    /**
     * URL-safe version of the name that can be used as part of a surrogate key to 
     * identify this instance.
     */
    @Column(name="`Slug`")
    private String slug;
    
    /**
     * The friendly name given to the vault.
     */
    @Column(name="`Name`")
    private String name;
    
    /**
     * The owner that this division belongs to
     */
    @ManyToOne
    @JoinColumn(name="`ActorID`")
    private Owner owner;
    

    public final String getSlug() {
        return slug;
    }

    public final void setSlug(String slug) {
        this.slug = slug;
    }

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final KeySafeStatus getStatus() {
        return status;
    }

    public final void setStatus(KeySafeStatus status) {
        this.status = status;
    }
    
    public final Owner getOwner() {
        return owner;
    }

    public final void setOwner(Owner owner) {
        this.owner = owner;
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
