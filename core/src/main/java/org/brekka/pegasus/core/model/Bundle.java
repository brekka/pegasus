/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.brekka.commons.persistence.model.SnapshotEntity;
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
     * The encryption initialisation vector use for the bundle XML.
     * Will be nulled out when the bundle is de-allocated
     */
    @Column(name="`IV`")
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
     * The list of files containened in the bundle
     */
    @OneToMany(mappedBy="bundle")
    @MapKey
    private Map<UUID, BundleFile> files;
    
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

    public Map<UUID, BundleFile> getFiles() {
        return files;
    }

    public void setFiles(Map<UUID, BundleFile> files) {
        this.files = files;
    }
}
