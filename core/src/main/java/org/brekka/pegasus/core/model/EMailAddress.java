/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.brekka.commons.persistence.model.SnapshotEntity;

/**
 * Encapsulates an e-mail address owned by a member (who can have more than one address). Note that the address itself is not
 * stored here, but a hash which can be used to lookup the address.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`EMailAddress`", uniqueConstraints={
        @UniqueConstraint(columnNames={ "`Hash`", "`Active`"})
})
public class EMailAddress extends SnapshotEntity {
    /**
     * Serial UID
     */
    private static final long serialVersionUID = 7519489574234136670L;

    /**
     * A hash of the e-mail address (SHA-256). May also have gone through a number of iterations.
     */
    @Column(name="`Hash`", length=32, nullable=false)
    private byte[] hash;
    
    /**
     * The domain the address belongs to (the part after the '@'). For common services such as gmail, this
     * will simply be null. It is intended to be used to aggregate organization members by their e-mail.
     */
    @ManyToOne
    @JoinColumn(name="`DomainName`")
    private DomainName domainName;
    
    /**
     * The member who owns this mail address.
     */
    @ManyToOne
    @JoinColumn(name="`Owner`", nullable=false)
    private Member owner;
    
    /**
     * Is this instance of the e-mail active or not. Once a single instance of a given hash can be
     * active at a time. If a user validates an e-mail to a new profile, then the old one becomes inactive.
     * 
     * Should never be set to false, just true or null (so that multiple validations can take place at a time).
     */
    @Column(name="`Active`")
    @Enumerated(EnumType.STRING)
    private Boolean active;
    
    /**
     * The timestamp of when the address was verified
     */
    @Column(name="`Verified`")
    @Temporal(TemporalType.TIMESTAMP)
    private Date verified;
    
    /**
     * The code used to verify this e-mail address. Normally null.
     */
    @Column(name="`VerificationCode`", length=32)
    private String verificationCode;
    
    /**
     * The address in its plain form. This will need to be extracted from an encrypted source
     * such as a user profile.
     */
    @Transient
    private transient String address;

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public DomainName getDomainName() {
        return domainName;
    }

    public void setDomainName(DomainName domainName) {
        this.domainName = domainName;
    }

    public Member getOwner() {
        return owner;
    }

    public void setOwner(Member owner) {
        this.owner = owner;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Date getVerified() {
        return verified;
    }

    public void setVerified(Date verified) {
        this.verified = verified;
    }
}
