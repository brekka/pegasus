/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;

/**
 * A vault is a place for a member to store various keys.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@DiscriminatorValue("Vault")
public class Vault extends KeySafe {
    
    /**
     * Serial UID
     */
    private static final long serialVersionUID = -5208658520688698466L;

    /**
     * The principal Id of this vault
     */
    @Type(type="pg-uuid")
    @Column(name="`PrincipalID`")
    private UUID principalId;
    
    /**
     * The owner of this vault
     */
    @ManyToOne
    @JoinColumn(name="`OwnerID`")
    private Member owner;
    

    public UUID getPrincipalId() {
        return principalId;
    }

    public void setPrincipalId(UUID principalId) {
        this.principalId = principalId;
    }
    
    public Member getOwner() {
        return owner;
    }

    public void setOwner(Member owner) {
        this.owner = owner;
    }
}
