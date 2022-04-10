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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.brekka.commons.persistence.model.EntityType;
import org.brekka.commons.persistence.model.SnapshotEntity;
import org.brekka.pegasus.core.PegasusConstants;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Type;

/**
 * Defines the relationship between an entity and a {@link Collective} that it has been assigned to.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name = "`Assignment`", schema = PegasusConstants.SCHEMA,
        uniqueConstraints=@UniqueConstraint(columnNames={"`EntityID`", "`EntityType`"}))
public class Assignment extends SnapshotEntity<UUID> {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 5110309434735285430L;

    /**
     * The id
     */
    @Id
    @AccessType("property")
    @Type(type = "pg-uuid")
    @Column(name = "`ID`")
    private UUID id;

    /**
     * The ID of the entity
     *
     * The is the byte representation of an entity id (type 4 UUID). At the time this was defined, the application
     * relied on automatic DDL generation and was missing the @Type(type="pg-uuid") annotation so this ended up being a
     * byte array.
     */
    @Column(name = "`EntityID`", nullable=false)
    private byte[] entityId;

    /**
     * The entity type (helps to work out where the entity came from). Not strictly necessary given the ID is universally unique.
     */
    @Column(name = "`EntityType`", length=32)
    @Type(type="org.brekka.pegasus.core.support.AllocationDispositionUserType")
    private EntityType entityType;

    /**
     * The collective assigned to this entity.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "`CollectiveID`", nullable=false)
    private Collective collective;


    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(final UUID id) {
        this.id = id;
    }

    /**
     * @return the entityId
     */
    public byte[] getEntityId() {
        return entityId;
    }

    /**
     * @param entityId the entityId to set
     */
    public void setEntityId(final byte[] entityId) {
        this.entityId = entityId;
    }

    /**
     * @return the entityType
     */
    public EntityType getEntityType() {
        return entityType;
    }

    /**
     * @param entityType the entityType to set
     */
    public void setEntityType(final EntityType entityType) {
        this.entityType = entityType;
    }

    /**
     * @return the collective
     */
    public Collective getCollective() {
        return collective;
    }

    /**
     * @param collective the collective to set
     */
    public void setCollective(final Collective collective) {
        this.collective = collective;
    }
}
