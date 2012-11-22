/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.brekka.commons.persistence.model.SnapshotEntity;
import org.brekka.pegasus.core.PegasusConstants;
import org.hibernate.annotations.Type;

/**
 * Represents an Internet domain name such as 'brekka.org'. 
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`DomainName`", schema=PegasusConstants.SCHEMA)
public class DomainName extends SnapshotEntity<UUID> {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -4101174769611796000L;

    /**
     * Unique id
     */
    @Id
    @Type(type="pg-uuid")
    @Column(name="`ID`")
    private UUID id;
    
    /**
     * A hash of the domain name (SHA-256). May also have gone through a number of iterations.
     */
    @Column(name="`Hash`", unique=true, length=32, nullable=false)
    private byte[] hash;
    
    /**
     * The address in its plain form.
     */
    @Transient
    private transient String address;

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

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
