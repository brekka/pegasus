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
import javax.persistence.UniqueConstraint;

/**
 * Keeps keys safe.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`KeySafe`",
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
public abstract class KeySafe extends LongevousEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -621238034501395611L;

    /**
     * Records the current status of this vault
     */
    @Column(name="`Status`", length=8, nullable=false)
    @Enumerated(EnumType.STRING)
    private KeySafeStatus status = KeySafeStatus.ACTIVE;
    
    @Column(name="`Slug`", nullable=false)
    private String slug;
    
    @Column(name="`Name`", nullable=false)
    private String name;
    

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public KeySafeStatus getStatus() {
        return status;
    }

    public void setStatus(KeySafeStatus status) {
        this.status = status;
    }
}
