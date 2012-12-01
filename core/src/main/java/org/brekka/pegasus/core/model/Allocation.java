/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.brekka.commons.persistence.model.SnapshotEntity;
import org.brekka.pegasus.core.PegasusConstants;
import org.brekka.phoenix.api.CryptoProfile;
import org.brekka.phoenix.api.SecretKey;
import org.brekka.phoenix.api.SymmetricCryptoSpec;
import org.brekka.xml.pegasus.v2.model.AllocationType;
import org.hibernate.annotations.Type;

/**
 * A bundle that has been allocated to something.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`Allocation`", schema=PegasusConstants.SCHEMA)
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="`Type`", length=10,
    discriminatorType=DiscriminatorType.STRING
)
@DiscriminatorValue("Allocation")
public abstract class Allocation extends SnapshotEntity<UUID> implements SymmetricCryptoSpec {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -949222719619428451L;
    
    /**
     * Unique id
     */
    @Id
    @Type(type="pg-uuid")
    @Column(name="`ID`")
    private UUID id;
    
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
     * Id of the crypted data that contains the key used to encrypt this file's parts.
     * Will be nulled-out once the bundle is de-allocated.
     */
    @Type(type="pg-uuid")
    @Column(name="`CryptedDataID`")
    private UUID cryptedDataId;
    
    /**
     * When is this allocation due to expire?
     */
    @Column(name="`Expires`")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expires;

    /**
     * Determines when/whether this allocation has been deleted.
     */
    @Column(name = "`Deleted`")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deleted;
    
    /**
     * The list of files contained in the allocation
     */
    @OneToMany(mappedBy="allocation")
    @MapKey(name="cryptedFileId")
    private Map<UUID, AllocationFile> files;
    
    /**
     * An allocation could be derived from a dispatch (in the case of a file sent by a member).
     */
    @ManyToOne
    @JoinColumn(name="DerivedFromID")
    private Dispatch derivedFrom;
    
    
    /**
     * If set to to true, this allocation will be purged upon successful download.
     */
    @Column(name="PurgeOnDownload")
    private Boolean purgeOnDownload;
    
    /**
     * Secret key for the allocation XML (transient).
     */
    @Transient
    private transient SecretKey secretKey;
    
    /**
     * The XML that backs this bundle. Transient as it will be encrypted and stored separately
     */
    @Transient
    private transient AllocationType xml;

    public final UUID getCryptedDataId() {
        return cryptedDataId;
    }

    public final void setCryptedDataId(UUID cryptedDataId) {
        this.cryptedDataId = cryptedDataId;
    }
    
    public int getProfile() {
        return profile;
    }

    public void setProfile(int profile) {
        this.profile = profile;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }
    
    public Map<UUID, AllocationFile> getFiles() {
        return files;
    }

    public void setFiles(Map<UUID, AllocationFile> files) {
        this.files = files;
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    public AllocationType getXml() {
        return xml;
    }

    public void setXml(AllocationType xml) {
        this.xml = xml;
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

    public Dispatch getDerivedFrom() {
        return derivedFrom;
    }

    public void setDerivedFrom(Dispatch derivedFrom) {
        this.derivedFrom = derivedFrom;
    }

    /**
     * @return the id
     */
    public final UUID getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public final void setId(UUID id) {
        this.id = id;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.phoenix.api.CryptoSpec#getCryptoProfile()
     */
    @Override
    public CryptoProfile getCryptoProfile() {
        return CryptoProfile.Static.of(getProfile());
    }
    
    /* (non-Javadoc)
     * @see org.brekka.phoenix.api.SymmetricCryptoSpec#getIV()
     */
    @Override
    public byte[] getIV() {
        return iv;
    }
}
