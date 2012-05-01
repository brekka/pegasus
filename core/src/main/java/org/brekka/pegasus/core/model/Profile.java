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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.brekka.xml.pegasus.v1.model.ProfileType;
import org.hibernate.annotations.Type;

/**
 * Contains the profile information for a user. Depending on what the user selects, this data may be stored encrypted
 * or in compressed plaintext.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="\"Profile\"")
public class Profile extends LongevousEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 7351357698414364086L;

    /**
     * The user that owns this profile.
     */
    @ManyToOne
    @JoinColumn(name="OwnerID", nullable=false)
    private Member owner;
    
    /**
     * If the profile should be encrypted, then this field should be set to the id of the vault.
     */
    @OneToOne
    @JoinColumn(name="VaultID")
    private Vault vault;
    
    /**
     * The encryption initialisation vector used for the profile encryption (if encrypted).
     */
    @Column(name="IV")
    private byte[] iv;
    
    /**
     * Id of the crypted data that contains the key used to encrypt the profile (if encrypted).
     */
    @Type(type="pg-uuid")
    @Column(name="CryptedDataID")
    private UUID cryptedDataId;
    
    /**
     * Should not be more than 100KB of compressed data. 
     * Use a blob so that the raw data does not remain in memory.
     */
    @Column(name="Data", length=100000, nullable=false)
    private Blob data;
    
    /**
     * The actual profile information in object form
     */
    @Transient
    private transient ProfileType xml;

    public Member getOwner() {
        return owner;
    }

    public void setOwner(Member owner) {
        this.owner = owner;
    }

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

    public ProfileType getXml() {
        return xml;
    }

    public void setXml(ProfileType xml) {
        this.xml = xml;
    }
}
