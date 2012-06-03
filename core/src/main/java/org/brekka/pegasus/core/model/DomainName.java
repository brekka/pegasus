/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.brekka.commons.persistence.model.SnapshotEntity;
import org.brekka.pegasus.core.PegasusConstants;

/**
 * Represents an internet domain name such as 'brekka.org'. 
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`DomainName`", schema=PegasusConstants.SCHEMA)
public class DomainName extends SnapshotEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -4101174769611796000L;

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
