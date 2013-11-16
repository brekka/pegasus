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
import javax.persistence.FetchType;
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
        // Owner and slug must be unique
        @UniqueConstraint(columnNames = {"`ActorID`", "`Slug`"}),
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
    @ManyToOne(targetEntity=Actor.class, fetch=FetchType.LAZY)
    @JoinColumn(name="`ActorID`")
    private Owner owner;


    public String getSlug() {
        return this.slug;
    }

    public void setSlug(final String slug) {
        this.slug = slug;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public KeySafeStatus getStatus() {
        return this.status;
    }

    public void setStatus(final KeySafeStatus status) {
        this.status = status;
    }

    public Owner getOwner() {
        return this.owner;
    }

    public void setOwner(final Owner owner) {
        this.owner = owner;
    }

    /**
     * @return the id
     */
    @Override
    public UUID getId() {
        return this.id;
    }

    /**
     * @param id the id to set
     */
    @Override
    public void setId(final UUID id) {
        this.id = id;
    }
}
