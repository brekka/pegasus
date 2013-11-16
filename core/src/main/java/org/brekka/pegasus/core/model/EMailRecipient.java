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

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.brekka.commons.persistence.model.SnapshotEntity;
import org.brekka.pegasus.core.PegasusConstants;
import org.hibernate.annotations.Type;

/**
 * The recipient of an E-Mail. An E-Mail can have multiple TO entries.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`EMailRecipient`", schema=PegasusConstants.SCHEMA)
public class EMailRecipient extends SnapshotEntity<UUID> {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -1088832577772756913L;

    /**
     * Unique id
     */
    @Id
    @Type(type="pg-uuid")
    @Column(name="`ID`")
    private UUID id;

    /**
     * The address of the recipient.
     */
    @ManyToOne
    @JoinColumn(name="`EMailAddressID`")
    private EMailAddress address;

    /**
     * The message sent to the recipient
     */
    @ManyToOne
    @JoinColumn(name="`EMailMessageID`")
    private EMailMessage message;

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

    /**
     * @return the address
     */
    public EMailAddress getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(final EMailAddress address) {
        this.address = address;
    }

    /**
     * @return the message
     */
    public EMailMessage getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(final EMailMessage message) {
        this.message = message;
    }
}
