/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.brekka.pegasus.core.model;

import java.util.List;
import java.util.UUID;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.brekka.commons.persistence.model.SnapshotEntity;
import org.brekka.pegasus.core.PegasusConstants;
import org.brekka.xml.pegasus.v2.model.EMailMessageDocument;
import org.hibernate.annotations.Type;

/**
 * An e-mail message
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`EMailMessage`", schema=PegasusConstants.SCHEMA)
public class EMailMessage extends SnapshotEntity<UUID> implements XmlEntityAware<EMailMessageDocument> {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -7530532580991812546L;

    /**
     * Unique id
     */
    @Id
    @Type(type="pg-uuid")
    @Access(AccessType.PROPERTY)
    @Column(name="`ID`")
    private UUID id;

    /**
     * The sender of the message, in case we want to locate messages by sender. Should be indexed
     */
    @ManyToOne
    @JoinColumn(name="`SenderEMailAddressID`")
    private EMailAddress sender;

    /**
     * The subject of the mail. Included for optimal listing / filtering performance (not having to open the xml). The DAO
     * should control whether this gets set or not.
     */
    @Column(name="`Subject`", length=4000, nullable=true)
    private String subject;

    /**
     * The actual user that created the message. Should also be indexed.
     */
    @ManyToOne
    @JoinColumn(name="`OwnerID`")
    private Actor owner;

    /**
     * The email content, that may be encrypted.
     */
    @OneToOne()
    @JoinColumn(name="`XmlEntityID`")
    private XmlEntity<EMailMessageDocument> xml;

    /**
     * The recipients
     */
    @OneToMany(mappedBy="message", fetch=FetchType.EAGER, cascade=CascadeType.ALL)
    private List<EMailRecipient> recipients;

    /**
     * The reference allocated by the service that will be relaying the message.
     */
    @Column(name="`Reference`")
    private String reference;

    /**
     * @return the recipients
     */
    public List<EMailRecipient> getRecipients() {
        return this.recipients;
    }

    /**
     * @param recipients the recipients to set
     */
    public void setRecipients(final List<EMailRecipient> recipients) {
        this.recipients = recipients;
    }

    /**
     * @return the sender
     */
    public EMailAddress getSender() {
        return this.sender;
    }

    /**
     * @param sender the sender to set
     */
    public void setSender(final EMailAddress sender) {
        this.sender = sender;
    }

    /**
     * @return the owner
     */
    public Actor getOwner() {
        return this.owner;
    }

    /**
     * @param owner the owner to set
     */
    public void setOwner(final Actor owner) {
        this.owner = owner;
    }

    /**
     * @return the xml
     */
    @Override
    public XmlEntity<EMailMessageDocument> getXml() {
        return this.xml;
    }

    /**
     * @param xml the xml to set
     */
    @Override
    public void setXml(final XmlEntity<EMailMessageDocument> xml) {
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
     * @return the reference
     */
    public String getReference() {
        return this.reference;
    }

    /**
     * @param reference the reference to set
     */
    public void setReference(final String reference) {
        this.reference = reference;
    }
}
