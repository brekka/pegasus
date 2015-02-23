/**
 *
 */
package org.brekka.pegasus.core.model;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.brekka.commons.persistence.annotations.ConditionalPersist;
import org.brekka.commons.persistence.model.SnapshotEntity;
import org.brekka.pegasus.core.PegasusConstants;
import org.hibernate.annotations.Type;

/**
 * Encapsulates an e-mail address owned by a member (who can have more than one address). Note that the cleartext address itself may not be
 * stored here, but a hash which can be used to lookup the address always will be (ensuring uniqueness).
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`EMailAddress`", schema=PegasusConstants.SCHEMA, uniqueConstraints={
        @UniqueConstraint(columnNames={ "`Hash`", "`Active`"})
})
public class EMailAddress extends SnapshotEntity<UUID> {
    /**
     * Serial UID
     */
    private static final long serialVersionUID = 7519489574234136670L;

    /**
     * Unique id
     */
    @Id
    @Type(type="pg-uuid")
    @Access(AccessType.PROPERTY)
    @Column(name="`ID`")
    private UUID id;

    /**
     * A hash of the e-mail address. The algorithm used depends on how the service is configured.
     */
    @Column(name="`Hash`", length=32, nullable=false)
    private byte[] hash;

    /**
     * The address in its plain form. Can optionally be persisted though the default is not to.
     */
    @Column(name="`ClearText`", length=256, nullable=true)
    @ConditionalPersist(copyFrom="address")
    private String clearText;

    /**
     * The domain the address belongs to (the part after the '@'). For common services such as gmail, this
     * may simply be null. It is intended to be used to aggregate organization members by their e-mail.
     */
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="`DomainName`")
    private DomainName domainName;

    /**
     * The member who owns this mail address. It may be that the e-mail does not belong to an active member
     * so allow nulls.
     */
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="`Owner`")
    private Member owner;

    /**
     * Is this instance of the e-mail active or not. Once a single instance of a given hash can be
     * active at a time. If a user validates an e-mail to a new profile, then the old one becomes inactive.
     *
     * Should never be set to false, just true or null (so that multiple validations can take place at a time).
     */
    @Column(name="`Active`")
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

    private transient String address;

    /**
     * @return the id
     */
    @Override
    public UUID getId() {
        return this.id;
    }

    /**
     * @param id the id to set
     */
    @Override
    public void setId(final UUID id) {
        this.id = id;
    }

    public byte[] getHash() {
        return this.hash;
    }

    public void setHash(final byte[] hash) {
        this.hash = hash;
    }

    public DomainName getDomainName() {
        return this.domainName;
    }

    public void setDomainName(final DomainName domainName) {
        this.domainName = domainName;
    }

    public Member getOwner() {
        return this.owner;
    }

    public void setOwner(final Member owner) {
        this.owner = owner;
    }

    public String getVerificationCode() {
        return this.verificationCode;
    }

    public void setVerificationCode(final String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(final String address) {
        this.address = address;
    }

    public Boolean getActive() {
        return this.active;
    }

    public void setActive(final Boolean active) {
        this.active = active;
    }

    public Date getVerified() {
        return this.verified;
    }

    public void setVerified(final Date verified) {
        this.verified = verified;
    }
}
