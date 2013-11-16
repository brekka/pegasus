/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.brekka.commons.persistence.model.IdentifiableEntity;
import org.hibernate.annotations.Type;

/**
 * An event executed by a remote user.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@MappedSuperclass
public abstract class RemoteUserEvent implements IdentifiableEntity<UUID> {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 497882239907363162L;

    /**
     * Unique id
     */
    @Id
    @Type(type="pg-uuid")
    @Column(name="`ID`")
    private UUID id;

    /**
     * The moment the event begun
     */
    @Column(name="`Initiated`", updatable=false, nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date initiated;

    /**
     * IP address of the system this web server talked to.
     */
    @Column(name="`RemoteAddress`", nullable=false)
    private String remoteAddress;

    /**
     * If the remote party was behind a proxy and it reported the internal IP,
     * this will record that IP.
     */
    @Column(name="`OnBehalfOfAddress`")
    private String onBehalfOfAddress;

    /**
     * The user agent reported by the remote server
     */
    @Column(name="`UserAgent`")
    private String userAgent;

    /**
     * The member who performed the event (if available).
     */
    @ManyToOne
    @JoinColumn(name="`MemberID`")
    private Member member;

    public Date getInitiated() {
        return initiated;
    }

    public void setInitiated(final Date initiated) {
        this.initiated = initiated;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(final String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getOnBehalfOfAddress() {
        return onBehalfOfAddress;
    }

    public void setOnBehalfOfAddress(final String onBehalfOfAddress) {
        this.onBehalfOfAddress = onBehalfOfAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(final String userAgent) {
        this.userAgent = userAgent;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(final Member member) {
        this.member = member;
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
}
