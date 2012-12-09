/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.sql.Blob;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.xmlbeans.XmlObject;
import org.brekka.commons.persistence.model.SnapshotEntity;
import org.brekka.pegasus.core.PegasusConstants;
import org.brekka.phoenix.api.CryptoProfile;
import org.brekka.phoenix.api.SecretKey;
import org.brekka.phoenix.api.SymmetricCryptoSpec;
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
     * 
     */
    public XmlEntity() {
    }
    
    /**
     * @param bean
     */
    public XmlEntity(T bean) {
        this.bean = bean;
    }

    /**
     * Unique id
     */
    @Id
    @Type(type="pg-uuid")
    @Column(name="`ID`")
    private UUID id;
    
    /**
     * A common key between versions
     */
    @Column(name="`SerialID`")
    private UUID serial;
    
    /**
     * Version number
     */
    @Column(name="`Version`")
    private int version;
    
    /**
     * If the XML should be encrypted, then this field should be set to the id of the key store.
     */
    @ManyToOne
    @JoinColumn(name="`KeySafeID`", updatable=false)
    private KeySafe keySafe;
    
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
     * Should not be more than 200KB of compressed data. 
     * Use a blob so that the raw data does not remain in memory.
     */
    @Column(name="`Data`", length=MAX_DATA_LENGTH, nullable=false, updatable=false)
    private Blob data;
    
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

    public KeySafe getKeySafe() {
        return keySafe;
    }

    public void setKeySafe(KeySafe keySafe) {
        this.keySafe = keySafe;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    public UUID getCryptedDataId() {
        return cryptedDataId;
    }

    public void setCryptedDataId(UUID cryptedDataId) {
        this.cryptedDataId = cryptedDataId;
    }

    public Blob getData() {
        return data;
    }

    public void setData(Blob data) {
        this.data = data;
    }

    public T getBean() {
        return bean;
    }

    public void setBean(T bean) {
        this.bean = bean;
    }

    public int getProfile() {
        return profile;
    }

    public void setProfile(int profile) {
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
    public void setSecretKey(SecretKey secretKey) {
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
    public void setSerial(UUID serial) {
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
    public void setVersion(int version) {
        this.version = version;
    }
    
    
}
