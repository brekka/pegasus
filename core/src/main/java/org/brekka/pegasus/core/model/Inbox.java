/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.brekka.commons.persistence.model.LongevousEntity;
import org.brekka.pegasus.core.PegasusConstants;
import org.hibernate.annotations.Type;

/**
 * An inbox allows an individual on the internet to send a file to a {@link Member}. The public key of the vault will be
 * used to store the key of the uploaded file.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`Inbox`", schema=PegasusConstants.SCHEMA)
public class Inbox extends LongevousEntity<UUID> {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 5531713690260375934L;

    /**
     * Unique id
     */
    @Id
    @Type(type="pg-uuid")
    @Column(name="`ID`")
    private UUID id;
    
    /**
     * The token that identifies this inbox to the outside world.
     */
    @OneToOne
    @JoinColumn(name="`TokenID`", nullable = false, unique=true)
    private Token token;

    /**
     * The key safe that will be used to store files added to this inbox. The user can change this at any time,
     * with future deposits being associated with the new key safe.
     */
    @ManyToOne
    @JoinColumn(name="`KeySafeID`", nullable = false)
    private KeySafe<?> keySafe;
    
    /**
     * The owner of this inbox. One of owner or division should be set.
     */
    @ManyToOne
    @JoinColumn(name="`OwnerID`")
    private Actor owner;
    
    /**
     * The division this inbox may belong to.
     */
    @ManyToOne
    @JoinColumn(name="`DivisionID`")
    private Division<?> division;
    
    /**
     * Text to be displayed to the person when depositing a file.
     */
    @Column(name="`Introduction`", length=2000)
    private String introduction;
    
    /**
     * Records the current status of this inbox
     */
    @Column(name="`Status`", length=8, nullable=false)
    @Enumerated(EnumType.STRING)
    private InboxStatus status = InboxStatus.ACTIVE;
    
    /**
     * The user can connect an e-mail address to the inbox as another way to identify it.
     */
    @OneToOne
    @JoinColumn(name="`EMailAddressID`", unique=true)
    private EMailAddress eMailAddress;
    
    /**
     * Name will be stored separately
     */
    @Transient
    private transient String name;
    
    
    
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

    public KeySafe<?> getKeySafe() {
        return keySafe;
    }

    public void setKeySafe(KeySafe<?> keySafe) {
        this.keySafe = keySafe;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public Actor getOwner() {
        return owner;
    }

    public void setOwner(Actor owner) {
        this.owner = owner;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InboxStatus getStatus() {
        return status;
    }

    public void setStatus(InboxStatus status) {
        this.status = status;
    }

    public Division<?> getDivision() {
        return division;
    }

    public void setDivision(Division<?> division) {
        this.division = division;
    }

    public EMailAddress geteMailAddress() {
        return eMailAddress;
    }

    public void seteMailAddress(EMailAddress eMailAddress) {
        this.eMailAddress = eMailAddress;
    }
}
