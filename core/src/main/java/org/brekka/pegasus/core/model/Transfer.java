/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Entity
@Table(name="`Transfer`")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="`Type`", length=9,
    discriminatorType=DiscriminatorType.STRING
)
@DiscriminatorValue("Transfer")
public abstract class Transfer extends SnapshotEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -6144176815399227699L;

    /**
     * The bundle represented by this deposit
     */
    @ManyToOne
    @JoinColumn(name="`BundleID`", updatable=false, nullable=false)
    private Bundle bundle;
    
    /**
     * Id of the crypted data that contains the key used to encrypt this file's parts.
     * Will be nulled-out once the bundle is de-allocated.
     */
    @Type(type="pg-uuid")
    @Column(name="`CryptedDataID`")
    private UUID cryptedDataId;
    
    public final Bundle getBundle() {
        return bundle;
    }

    public final void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }
    
    
    public final UUID getCryptedDataId() {
        return cryptedDataId;
    }

    public final void setCryptedDataId(UUID cryptedDataId) {
        this.cryptedDataId = cryptedDataId;
    }
}
