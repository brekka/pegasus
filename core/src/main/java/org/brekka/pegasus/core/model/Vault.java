/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.brekka.commons.persistence.model.IdentifiableEntity;
import org.hibernate.annotations.Type;

/**
 * @author Andrew Taylor
 *
 */
@Entity
@Table(name="\"Vault\"", uniqueConstraints={ 
        @UniqueConstraint(columnNames = {"OwnerID", "Token"}) 
    }
)
public class Vault extends IdentifiableEntity {
    
    /**
     * Serial UID
     */
    private static final long serialVersionUID = -5208658520688698466L;

    @ManyToOne
    @JoinColumn(name="OwnerID", nullable=false)
    private Member owner;
    
    @Column(name="Token", nullable=false)
    private String token;
    
    @Column(name="Name")
    private String name;
    
    /**
     * The principal Id of this membe
     */
    @Type(type="pg-uuid")
    @Column(name="PrincipalID", nullable=false)
    private UUID principalId;

    public Member getOwner() {
        return owner;
    }

    public void setOwner(Member owner) {
        this.owner = owner;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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
}
