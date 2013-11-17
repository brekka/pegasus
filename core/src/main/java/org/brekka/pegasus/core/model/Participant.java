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
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.brekka.commons.persistence.model.SnapshotEntity;
import org.brekka.pegasus.core.PegasusConstants;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Type;

/**
 * A member of a given collective.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name = "`Participant`", schema = PegasusConstants.SCHEMA)
public class Participant extends SnapshotEntity<UUID> {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 356915825588300697L;

    /**
     * The id
     */
    @Id
    @AccessType("property")
    @Type(type = "pg-uuid")
    @Column(name = "`ID`")
    private UUID id;

    /**
     * The collective that we are a member of.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`CollectiveID`", nullable=false)
    private Collective collective;

    /**
     * The member who has been added to the collective
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`MemberID`", nullable=false)
    private Member member;

    /**
     * Stores the connection between the the {@link Member#getPrimaryKeySafe()} and the
     * division held by the {@link Inbox} in the {@link Collective}, assuming there is one.
     * If an inbox is present, and no partnership existsm, assume the inbox keysafe = primaryKeySafe.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`PartnershipID`")
    private Partnership<? extends Member, Actor> partnership;

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

    public Collective getCollective() {
        return this.collective;
    }

    public void setCollective(final Collective collective) {
        this.collective = collective;
    }

    public Member getMember() {
        return this.member;
    }

    public void setMember(final Member member) {
        this.member = member;
    }

    /**
     * @return the partnership
     */
    public Partnership<? extends Member, Actor> getPartnership() {
        return this.partnership;
    }

    /**
     * @param partnership the partnership to set
     */
    public void setPartnership(final Partnership<? extends Member, Actor> partnership) {
        this.partnership = partnership;
    }
}