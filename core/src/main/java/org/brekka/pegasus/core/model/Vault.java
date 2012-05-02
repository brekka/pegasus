/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Type;

/**
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(
    uniqueConstraints={ 
        @UniqueConstraint(columnNames = {"OwnerID", "Slug"}) 
    }
)
@DiscriminatorValue("Vault")
public class Vault extends CryptoStore {
    
    /**
     * Serial UID
     */
    private static final long serialVersionUID = -5208658520688698466L;

    @ManyToOne
    @JoinColumn(name="`OwnerID`", nullable=false)
    private Member owner;
    
    @Column(name="`Slug`", nullable=false)
    private String slug;
    
    @Column(name="`Name`")
    private String name;
    
    /**
     * The principal Id of this member
     */
    @Type(type="pg-uuid")
    @Column(name="`PrincipalID`", nullable=false)
    private UUID principalId;
    
    /**
     * Records the current status of this vault
     */
    @Column(name="`Status`", length=8, nullable=false)
    @Enumerated(EnumType.STRING)
    private VaultStatus status = VaultStatus.ACTIVE;
    
    
    public Member getOwner() {
        return owner;
    }

    public void setOwner(Member owner) {
        this.owner = owner;
    }

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

    public UUID getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(UUID principalId) {
        this.principalId = principalId;
    }

    public VaultStatus getStatus() {
        return status;
    }

    public void setStatus(VaultStatus status) {
        this.status = status;
    }
}
