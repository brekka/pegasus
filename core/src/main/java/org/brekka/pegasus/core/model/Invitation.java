/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.Date;
import java.util.UUID;

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
    @Column(name="`ID`")
    private UUID id;
    
    /**
     * Token that identifies this invitation
     */
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="`TokenID`")
    private Token token;
    
    /**
     * User that sent the invitation
     */
    @ManyToOne
    @JoinColumn(name="`SenderID`")
    private Actor sender;
    
    /**
     * Who should receive the invitation, assuming they are already a member. If not specified then the XML should
     * be encrypted with a password.
     */
    @ManyToOne
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
    @OneToOne
    @JoinColumn(name="`XmlEntityID`")
    private XmlEntity<InvitationDocument> xml;
   
    
    public Actor getSender() {
        return sender;
    }

    public void setSender(Actor sender) {
        this.sender = sender;
    }

    public Member getRecipient() {
        return recipient;
    }

    public void setRecipient(Member recipient) {
        this.recipient = recipient;
    }

    public Date getActioned() {
        return actioned;
    }

    public void setActioned(Date actioned) {
        this.actioned = actioned;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public void setStatus(InvitationStatus invitationStatus) {
        this.status = invitationStatus;
    }

    public XmlEntity<InvitationDocument> getXml() {
        return xml;
    }

    public void setXml(XmlEntity<InvitationDocument> xml) {
        this.xml = xml;
    }

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

    /**
     * @return the token
     */
    public Token getToken() {
        return token;
    }

    /**
     * @param token the token to set
     */
    public void setToken(Token token) {
        this.token = token;
    }
}
