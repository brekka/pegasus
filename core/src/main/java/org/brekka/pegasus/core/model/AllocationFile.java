/**
 *
 */
package org.brekka.pegasus.core.model;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.brekka.commons.persistence.model.IdentifiableEntity;
import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.pegasus.core.PegasusConstants;
import org.brekka.xml.pegasus.v2.model.FileType;
import org.hibernate.annotations.Type;

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
@Table(name = "`AllocationFile`", schema=PegasusConstants.SCHEMA)
public class AllocationFile implements IdentifiableEntity<UUID> {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -5007642578887170101L;

    /**
     * Unique id
     */
    @Id
    @Type(type="pg-uuid")
    @Access(AccessType.PROPERTY)
    @Column(name="`ID`")
    private UUID id;

    /**
     * The transfer that this file is part of
     */
    @ManyToOne()
    @JoinColumn(name = "`AllocationID`")
    private Allocation allocation;

    /**
     * The corresponding crypted file. Multiple AllocationFiles may reference the same crypted file.
     */
    @ManyToOne()
    @JoinColumn(name="`CryptedFileID`")
    private CryptedFile cryptedFile;

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
     * Which allocation file is this derived from
     */
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="`DerivedFromID`")
    private AllocationFile derivedFrom;

    /**
     * A reference to the XML model that backs this file (assuming it has been decrypted).
     */
    @Transient
    private transient FileType xml;

    @Transient
    private transient float progress;

    /**
     *
     */
    public AllocationFile() {
    }

    public AllocationFile(final UUID id, final Allocation allocation) {
        this.id = id;
        this.allocation = allocation;
    }

    public Date getDeleted() {
        return this.deleted;
    }

    public void setDeleted(final Date deleted) {
        this.deleted = deleted;
    }

    public FileType getXml() {
        return this.xml;
    }

    public void setXml(final FileType xml) {
        this.xml = xml;
    }

    public Date getExpires() {
        return this.expires;
    }

    public void setExpires(final Date expires) {
        this.expires = expires;
    }

    public int getDownloadCount() {
        return this.downloadCount;
    }

    public void setDownloadCount(final int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public Allocation getAllocation() {
        return this.allocation;
    }

    public void setAllocation(final Allocation allocation) {
        this.allocation = allocation;
    }

    /**
     * @return the cryptedFile
     */
    public CryptedFile getCryptedFile() {
        return this.cryptedFile;
    }

    /**
     * @param cryptedFile the cryptedFile to set
     */
    public void setCryptedFile(final CryptedFile cryptedFile) {
        this.cryptedFile = cryptedFile;
    }

    public float getProgress() {
        return this.progress;
    }

    public void setProgress(final float progress) {
        this.progress = progress;
    }

    /**
     * @return the id
     */
    @Override
    public UUID getId() {
        return this.id;
    }

    /**
     * @param id the id to set
     */
    @Override
    public void setId(final UUID id) {
        this.id = id;
    }

    /**
     * @return the derivedFrom
     */
    public AllocationFile getDerivedFrom() {
        return this.derivedFrom;
    }

    /**
     * @param derivedFrom the derivedFrom to set
     */
    public void setDerivedFrom(final AllocationFile derivedFrom) {
        this.derivedFrom = derivedFrom;
    }
}
