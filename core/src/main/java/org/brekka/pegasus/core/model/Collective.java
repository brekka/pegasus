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
import javax.persistence.UniqueConstraint;

import org.brekka.commons.persistence.model.LongevousEntity;
import org.brekka.pegasus.core.PegasusConstants;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Type;

/**
 * A named group of representatives within a specific client.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name = "`Collective`", schema = PegasusConstants.SCHEMA,
       uniqueConstraints=@UniqueConstraint(columnNames={"`OwnerID`", "`Key`"}))
public class Collective extends LongevousEntity<UUID> {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 667545779142431872L;

    /**
     * The id
     */
    @Id
    @AccessType("property")
    @Type(type = "pg-uuid")
    @Column(name = "`ID`")
    private UUID id;

    /**
     * The actor that this collective belongs to (ie organization, person etc). Doesn't have to be specified.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`OwnerID`")
    private Actor owner;

    /**
     * Inbox that allows this collective to receive files.
     * Doesn't have to be specified
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`InboxID`")
    private Inbox inbox;

    /**
     * Used along with the owner id to form a surrogate key.
     */
    @Column(name="`Key`", length=16)
    private String key;

    /**
     * Name of this collective (should be provided).
     */
    @Column(name="`Name`", length=255)
    private String name;

    /**
     * Optional description
     */
    @Column(name="`Description`", length=4000)
    private String description;

    /**
     * Is this a personal group, ie contains just the owner?
     */
    @Column(name="`Personal`")
    private boolean personal;

    /**
     * Who are in this collective.
     */
    @OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.REMOVE, mappedBy="collective")
    private List<Participant> participants;

    /**
     * What entities are assigned to this collective.
     */
    @OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.REMOVE, mappedBy="collective")
    private List<Assignment> assignments;

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.model.IdentifiableEntity#getId()
     */
    @Override
    public UUID getId() {
        return this.id;
    }

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.model.IdentifiableEntity#setId(java.io.Serializable)
     */
    @Override
    public void setId(final UUID id) {
        this.id = id;
    }


    public String getKey() {
        return this.key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Inbox getInbox() {
        return this.inbox;
    }

    public void setInbox(final Inbox inbox) {
        this.inbox = inbox;
    }

    public Actor getOwner() {
        return this.owner;
    }

    public void setOwner(final Actor owner) {
        this.owner = owner;
    }

    public boolean isPersonal() {
        return this.personal;
    }

    public void setPersonal(final boolean personal) {
        this.personal = personal;
    }
}