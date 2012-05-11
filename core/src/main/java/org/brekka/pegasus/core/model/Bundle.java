/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.brekka.xml.pegasus.v1.model.BundleType;

/**
 * A bundle represents a collection of files that should be transferred from one entity to another.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`Bundle`")
public class Bundle extends SnapshotEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -5103824363711199962L;
    
    /**
     * Crypto profile used for this file
     */
    @Column(name="`Profile`", nullable=false)
    private int profile;
    
    /**
     * The encryption initialisation vector use for the bundle XML
     */
    @Column(name="`IV`", nullable=false)
    private byte[] iv;
    
    /**
     * When does this bundle expire?
     */
    @Column(name="`Expires`")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expires;
    
    /**
     * When was this bundle actually deleted.
     */
    @Column(name="`Deleted`")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deleted;
    
    /**
     * The XML that backs this bundle. Transient as it will be encrypted and stored separately
     */
    @Transient
    private transient BundleType xml;

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public Date getDeleted() {
        return deleted;
    }

    public void setDeleted(Date deleted) {
        this.deleted = deleted;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    public int getProfile() {
        return profile;
    }

    public void setProfile(int profile) {
        this.profile = profile;
    }

    public BundleType getXml() {
        return xml;
    }

    public void setXml(BundleType xml) {
        this.xml = xml;
    }
}
