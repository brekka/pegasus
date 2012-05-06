/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

/**
 * Enables an anonymous transfer
 * 
 * @author Andrew Taylor
 */
@Entity
@Table(name="`AnonymousTransfer`")
public class AnonymousTransfer extends SnapshotEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -4542707737980018991L;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="`TokenID`")
    private Token token;
    
    @OneToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="`BundleID`")
    private Bundle bundle;

    /**
     * Id of the crypted data that contains the key used to encrypt the bundle
     */
    @Type(type="pg-uuid")
    @Column(name="`CryptedDataID`")
    private UUID cryptedDataId;

    
    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }
    
    public UUID getCryptedDataId() {
        return cryptedDataId;
    }

    public void setCryptedDataId(UUID cryptedDataId) {
        this.cryptedDataId = cryptedDataId;
    }
}
