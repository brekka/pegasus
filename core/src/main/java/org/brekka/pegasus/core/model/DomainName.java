/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
     * A hash of the domain name.
     */
    @Column(name="`Hash`", unique=true, length=32, nullable=false)
    private byte[] hash;

    /**
     * The domain in its plain form. Can be persisted if configured to do so.
     */
    @Column(name="`ClearText`", length=128, nullable=true)
    private String clearText;


    private transient String address;

    /**
     * @return the id
     */
    @Override
    public UUID getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    @Override
    public void setId(final UUID id) {
        this.id = id;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(final byte[] hash) {
        this.hash = hash;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(final String address) {
        this.address = address;
    }
}
