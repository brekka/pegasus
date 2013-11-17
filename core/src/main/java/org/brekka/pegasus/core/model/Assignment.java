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
     */
    @Column(name = "`EntityID`", nullable=false)
    private UUID entityId;
    
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

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.model.IdentifiableEntity#getId()
     */
    @Override
    public UUID getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.model.IdentifiableEntity#setId(java.io.Serializable)
     */
    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * @return the entityId
     */
    public UUID getEntityId() {
        return entityId;
    }

    /**
     * @param entityId the entityId to set
     */
    public void setEntityId(UUID entityId) {
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
    public void setEntityType(EntityType entityType) {
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
    public void setCollective(Collective collective) {
        this.collective = collective;
    }
}
