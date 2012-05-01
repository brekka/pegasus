/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.brekka.xml.pegasus.v1.model.BundleType;
import org.hibernate.annotations.Type;

/**
 * @author Andrew Taylor
 *
 */
@Entity
@Table(name="\"Bundle\"")
public class Bundle extends SnapshotEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -5103824363711199962L;

    /**
     * Id of the crypted data that contains the key used to encrypt this file's parts.
     */
    @Type(type="pg-uuid")
    @Column(name="CryptedDataID")
    private UUID cryptedDataId;
    
    /**
     * Crypto profile used for this file
     */
    @Column(name="Profile")
    private int profile;
    
    /**
     * The encryption initialisation vector use for the bundle XML
     */
    @Column(name="IV", nullable=false)
    private byte[] iv;
    
    /**
     * When does this bundle expire?
     */
    @Column(name="Expires")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expires;
    
    /**
     * When was this bundle actually deleted.
     */
    @Column(name="Deleted")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deleted;
    
    /**
     * The XML that backs this bundle. Transient as it will be encrypted and stored separately
     */
    @Transient
    private transient BundleType xml;
    
    public UUID getCryptedDataId() {
        return cryptedDataId;
    }

    public void setCryptedDataId(UUID cryptedDataId) {
        this.cryptedDataId = cryptedDataId;
    }

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
