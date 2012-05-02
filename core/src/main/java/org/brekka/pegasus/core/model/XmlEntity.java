/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.sql.Blob;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.xmlbeans.XmlObject;
import org.hibernate.annotations.Type;

/**
 * Stores a piece of XML either plain or encrypted. Should only ever be replaced, never updated.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`XmlEntity`")
public class XmlEntity<T extends XmlObject> extends SnapshotEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 1708978401887395862L;

    /**
     * If the XML should be encrypted, then this field should be set to the id of the vault.
     */
    @ManyToOne
    @JoinColumn(name="`VaultID`", nullable=false, updatable=false)
    private Vault vault;
    
    /**
     * The encryption initialisation vector used for the profile encryption (if encrypted).
     */
    @Column(name="`IV`", nullable=false, updatable=false)
    private byte[] iv;
    
    /**
     * Id of the crypted data that contains the key used to encrypt the profile (if encrypted).
     */
    @Type(type="pg-uuid")
    @Column(name="`CryptedDataID`", nullable=false, updatable=false)
    private UUID cryptedDataId;
    
    /**
     * Should not be more than 200KB of compressed data. 
     * Use a blob so that the raw data does not remain in memory.
     */
    @Column(name="`Data`", length=200000, nullable=false, updatable=false)
    private Blob data;
    
    /**
     * The actual profile information in object form
     */
    @Transient
    private transient T bean;
    
    

    public Vault getVault() {
        return vault;
    }

    public void setVault(Vault vault) {
        this.vault = vault;
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

}
