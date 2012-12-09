/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.brekka.commons.persistence.model.LongevousEntity;
import org.brekka.pegasus.core.PegasusConstants;
import org.hibernate.annotations.Type;

/**
 * Defines the relationship between an associate and a division within an organization.
 * Essentially it stores the private key that can be used to unlock resources bound to this
 * division and its children.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`DivisionAssociate`", schema=PegasusConstants.SCHEMA,
    uniqueConstraints={ 
        // Surrogate key
        @UniqueConstraint(columnNames = {"`DivisionID`", "`AssociateID`" }),
    }
)
public class DivisionAssociate extends LongevousEntity<UUID> {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 4972021130097729646L;

    /**
     * Unique id
     */
    @Id
    @Type(type="pg-uuid")
    @Column(name="`ID`")
    private UUID id;
    
    /**
     * The division that the associate is a member of
     */
    @ManyToOne
    @JoinColumn(name="`DivisionID`")
    private Division division;
    
    /**
     * The associate
     */
    @ManyToOne
    @JoinColumn(name="`AssociateID`")
    private Associate associate;
    
    
    /**
     * The vault that is storing the keyPair.
     */
    @OneToOne
    @JoinColumn(name="`VaultID`")
    private Vault vault;

    
    /**
     * The key pair that gives the associate access to the contents of this division.
     */
    @Column(name="`KeyPairID`")
    @Type(type="pg-uuid")
    private UUID keyPairId;
    
    /**
     * @return the id
     */
    public UUID getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(UUID id) {
        this.id = id;
    }

    public Division getDivision() {
        return division;
    }

    public void setDivision(Division division) {
        this.division = division;
    }

    public Associate getAssociate() {
        return associate;
    }

    public void setAssociate(Associate associate) {
        this.associate = associate;
    }

    public UUID getKeyPairId() {
        return keyPairId;
    }

    public void setKeyPairId(UUID keyPairId) {
        this.keyPairId = keyPairId;
    }

    /**
     * @return the vault
     */
    public Vault getVault() {
        return vault;
    }

    /**
     * @param vault the vault to set
     */
    public void setVault(Vault vault) {
        this.vault = vault;
    }
}
