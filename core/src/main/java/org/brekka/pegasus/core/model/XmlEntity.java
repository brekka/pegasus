/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.sql.Blob;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import net.iharder.Base64;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.xmlbeans.XmlObject;
import org.brekka.commons.persistence.model.SnapshotEntity;
import org.brekka.paveway.core.services.ResourceStorageService;
import org.brekka.pegasus.core.PegasusConstants;
import org.brekka.phoenix.api.CryptoProfile;
import org.brekka.phoenix.api.SecretKey;
import org.brekka.phoenix.api.SymmetricCryptoSpec;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

/**
 * Stores a piece of XML either plain or encrypted. Should only ever be replaced, never updated.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`XmlEntity`", schema=PegasusConstants.SCHEMA, uniqueConstraints=
@UniqueConstraint(columnNames={"`SerialID`", "`Version`"}))
public class XmlEntity<T extends XmlObject> extends SnapshotEntity<UUID> implements SymmetricCryptoSpec {

    /**
     * The maximum length of data that this entity can store.
     */
    public static final int MAX_DATA_LENGTH = 200000;

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 1708978401887395862L;

    /**
     * Unique id
     */
    @Id
    @Type(type="pg-uuid")
    @Column(name="`ID`", updatable=false)
    private UUID id;

    /**
     * A common key between versions
     */
    @Index(name="IDX_XmlEntity_Serial")
    @Column(name="`SerialID`", updatable=false, nullable=false)
    private UUID serial;

    /**
     * Version number
     */
    @Column(name="`Version`", updatable=false, nullable=false)
    private int version;

    /**
     * If the XML should be encrypted, then this field should be set to the id of the key store.
     */
    @ManyToOne
    @JoinColumn(name="`KeySafeID`", updatable=false)
    private KeySafe<?> keySafe;

    /**
     * Crypto profile used for this file
     */
    @Column(name="`Profile`", updatable=false)
    private int profile;

    /**
     * The encryption initialization vector used for the profile encryption (if encrypted).
     */
    @Column(name="`IV`", updatable=false)
    private byte[] iv;

    /**
     * Id of the crypted data that contains the key used to encrypt the profile (if encrypted).
     */
    @Type(type="pg-uuid")
    @Column(name="`CryptedDataID`", updatable=false)
    private UUID cryptedDataId;

    /**
     * Is the data external or local?
     */
    @Column(name="`ExternalData`", updatable=false)
    private boolean externalData;

    /**
     * Should not be more than 200KB of compressed data.
     * Use a blob so that the raw data does not remain in memory. If null then the content will be
     * held in the {@link ResourceStorageService}.
     */
    @Column(name="`Data`", length=MAX_DATA_LENGTH, updatable=false)
    private Blob data;

    /**
     * XML entities are initially marked soft-deleted and then completely removed at a later date.
     */
    @Column(name = "`Deleted`")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deleted;

    /**
     * The actual profile information in object form
     */
    @Transient
    private transient T bean;

    /**
     * Secret key
     */
    @Transient
    private transient SecretKey secretKey;


    /**
     * 
     */
    public XmlEntity() {
    }

    /**
     * @param bean
     */
    public XmlEntity(final T bean) {
        this.bean = bean;
    }

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

    public KeySafe<?> getKeySafe() {
        return keySafe;
    }

    public void setKeySafe(final KeySafe<?> keySafe) {
        this.keySafe = keySafe;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(final byte[] iv) {
        this.iv = iv;
    }

    public UUID getCryptedDataId() {
        return cryptedDataId;
    }

    public void setCryptedDataId(final UUID cryptedDataId) {
        this.cryptedDataId = cryptedDataId;
    }

    public Blob getData() {
        return data;
    }

    public void setData(final Blob data) {
        this.data = data;
    }

    public T getBean() {
        return bean;
    }

    public void setBean(final T bean) {
        this.bean = bean;
    }

    public int getProfile() {
        return profile;
    }

    public void setProfile(final int profile) {
        this.profile = profile;
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

    /* (non-Javadoc)
     * @see org.brekka.phoenix.api.SymmetricCryptoSpec#getSecretKey()
     */
    @Override
    public SecretKey getSecretKey() {
        return secretKey;
    }

    /**
     * @param secretKey the secretKey to set
     */
    public void setSecretKey(final SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * @return the serial
     */
    public UUID getSerial() {
        return serial;
    }

    /**
     * @param serial the serial to set
     */
    public void setSerial(final UUID serial) {
        this.serial = serial;
    }

    /**
     * @return the version
     */
    public int getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(final int version) {
        this.version = version;
    }

    /**
     * @return the externalData
     */
    public boolean isExternalData() {
        return externalData;
    }

    /**
     * @param externalData the externalData to set
     */
    public void setExternalData(final boolean externalData) {
        this.externalData = externalData;
    }

    /**
     * @return the deleted
     */
    public Date getDeleted() {
        return deleted;
    }

    /**
     * @param deleted the deleted to set
     */
    public void setDeleted(final Date deleted) {
        this.deleted = deleted;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("id", id)
        .append("profile", profile)
        .append("version", version)
        .append("external", externalData)
        .append("cryptedDataId", cryptedDataId)
        .append("keySafe", (keySafe != null ? keySafe.getId() : null))
        .append("iv", Base64.encodeBytes(iv))
        .toString();
    }
}
