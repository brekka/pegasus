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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.brekka.commons.persistence.model.SnapshotEntity;
import org.brekka.pegasus.core.PegasusConstants;
import org.brekka.xml.pegasus.v2.model.InvitationDocument;
import org.hibernate.annotations.Type;

/**
 * An invitation is a way to grant someone access to something in secure way. For example it can be used to invite a new user to the system
 * or give a member access to a division that they did not previously have access to.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`Invitation`", schema=PegasusConstants.SCHEMA)
public class Invitation extends SnapshotEntity<UUID> {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 6635175785485701951L;

    /**
     * Unique id
     */
    @Id
    @Type(type="pg-uuid")
    @Access(AccessType.PROPERTY)
    @Column(name="`ID`")
    private UUID id;

    /**
     * Token that identifies this invitation
     */
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="`TokenID`")
    private Token token;

    /**
     * User that sent the invitation
     */
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="`SenderID`")
    private Actor sender;

    /**
     * Who should receive the invitation, assuming they are already a member. If not specified then the XML should
     * be encrypted with a password.
     */
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="`RecipientID`")
    private Member recipient;

    /**
     * When did the invitation get actioned.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="`Actioned`")
    private Date actioned;

    /**
     * Status of the invitation
     */
    @Column(name="`Status`", nullable=false)
    @Enumerated(EnumType.STRING)
    private InvitationStatus status = InvitationStatus.NEW;

    /**
     * The details of the invitation that can potentially be encrypted.
     */
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="`XmlEntityID`")
    private XmlEntity<InvitationDocument> xml;


    public Actor getSender() {
        return this.sender;
    }

    public void setSender(final Actor sender) {
        this.sender = sender;
    }

    public Member getRecipient() {
        return this.recipient;
    }

    public void setRecipient(final Member recipient) {
        this.recipient = recipient;
    }

    public Date getActioned() {
        return this.actioned;
    }

    public void setActioned(final Date actioned) {
        this.actioned = actioned;
    }

    public InvitationStatus getStatus() {
        return this.status;
    }

    public void setStatus(final InvitationStatus invitationStatus) {
        this.status = invitationStatus;
    }

    public XmlEntity<InvitationDocument> getXml() {
        return this.xml;
    }

    public void setXml(final XmlEntity<InvitationDocument> xml) {
        this.xml = xml;
    }

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

    /**
     * @return the token
     */
    public Token getToken() {
        return this.token;
    }

    /**
     * @param token the token to set
     */
    public void setToken(final Token token) {
        this.token = token;
    }
}
