/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.brekka.commons.persistence.model.IdentifiableEntity;
import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.xml.pegasus.v1.model.FileType;

/**
 * Stores the relationship between a bundle and {@link CryptedFile}. The id will match that of a corresponding
 * CryptedFile entry (from Paveway).
 * 
 * While this information is available from the bundle XML, that will unavailable to the de-allocation logic (when the
 * bundle files are deleted).
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name = "`BundleFile`")
public class BundleFile extends IdentifiableEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -5007642578887170101L;

    /**
     * The bundle that this file is part of
     */
    @ManyToOne
    @JoinColumn(name = "`BundleID`")
    private Bundle bundle;
    
    /**
     * When did this bundle file expire?
     */
    @Column(name="`Expires`")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expires;

    /**
     * A file within a bundle can be deleted the moment is is downloaded. Note the fact the file has been deleted so
     * that the user can be informed it is no longer available.
     */
    @Column(name = "`Deleted`")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deleted;
    
    /**
     * The number of times this file has been downloaded
     */
    @Column(name = "`DownloadCount`", nullable=false)
    private int downloadCount = 0;
    
    /**
     * A reference to the XML model that backs this file (assuming it has been decrypted).
     */
    @Transient
    private transient FileType xml;

    /**
     * 
     */
    public BundleFile() {
    }

    public BundleFile(UUID id, Bundle bundle) {
        super(id);
        this.bundle = bundle;
    }

    public Date getDeleted() {
        return deleted;
    }

    public void setDeleted(Date deleted) {
        this.deleted = deleted;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public FileType getXml() {
        return xml;
    }

    public void setXml(FileType xml) {
        this.xml = xml;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }
}
