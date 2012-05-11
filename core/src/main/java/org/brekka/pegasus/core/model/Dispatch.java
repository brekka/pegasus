/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

/**
 * A bundle that has been or is in the process of being dispatched. Essentially exists as a way for
 * a member/employee to track what happened to the file they sent and potentially re-download it or
 * send it again.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`Dispatch`")
public class Dispatch extends SnapshotEntity {
    /**
     * Serial UID
     */
    private static final long serialVersionUID = -9040394712700014948L;
    
    /**
     * The employee / actor that sent this.
     */
    @ManyToOne
    @JoinColumn(name="`ActorID`")
    private Actor actor;
    
    /**
     * The division this from which this was dispatched.
     */
    @ManyToOne
    @JoinColumn(name="`DivisionID`")
    private Division division;
    
    /**
     * The key safe that contains the key
     */
    @ManyToOne
    @JoinColumn(name="`KeySafeID`", nullable=false, updatable=false)
    private KeySafe keySafe;
    
    /**
     * The bundle that was sent.
     */
    @OneToOne
    @JoinColumn(name="`BundleID`", nullable=false, updatable=false)
    private Bundle bundle;
    
    /**
     * Id of the phalanx data item containing the key used to encrypt the bundle. It can be decrypted using
     * the private key of the keySafe.
     */
    @Type(type="pg-uuid")
    @Column(name="`CryptedDataID`", nullable=false, updatable=false)
    private UUID cryptedDataId;

    public Actor getActor() {
        return actor;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public KeySafe getKeySafe() {
        return keySafe;
    }

    public void setKeySafe(KeySafe keySafe) {
        this.keySafe = keySafe;
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

    public Division getDivision() {
        return division;
    }

    public void setDivision(Division division) {
        this.division = division;
    }
}
