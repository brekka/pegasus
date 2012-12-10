/*
 * Copyright 2012 the original author or authors.
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
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.brekka.commons.persistence.model.LongevousEntity;
import org.brekka.pegasus.core.PegasusConstants;
import org.hibernate.annotations.Type;

/**
 * A connection between two divisions. The divisions do not necessarily have to be part of the same organization.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`Connection`", schema=PegasusConstants.SCHEMA,
    uniqueConstraints={ 
        // Surrogate key
        @UniqueConstraint(columnNames = {"`OwnerID`", "`SourceID`", "`TargetID`" }),
    }
)
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="`Type`", length=11,
    discriminatorType=DiscriminatorType.STRING
)
@DiscriminatorValue("Connection")
public class Connection<Owner extends Actor, Source extends KeySafe<?>, Target extends KeySafe<?>> extends LongevousEntity<UUID> {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 4424537324032693339L;

    /**
     * Unique id
     */
    @Id
    @Type(type="pg-uuid")
    @Column(name="`ID`")
    private UUID id;
    
    /**
     * Who owns the connection
     */
    @OneToOne
    @JoinColumn(name="`OwnerID`")
    private Owner owner;
    
    /**
     * The source
     */
    @ManyToOne
    @JoinColumn(name="`SourceID`")
    private Source source;
    
    /**
     * The target
     */
    @ManyToOne
    @JoinColumn(name="`TargetID`")
    private Target target;
    
    /**
     * The key pair that gives the source division access to the target. 
     */
    @Column(name="`KeyPairID`")
    @Type(type="pg-uuid")
    private UUID keyPairId;

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
     * @return the owner
     */
    public Owner getOwner() {
        return owner;
    }

    /**
     * @param owner the owner to set
     */
    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    /**
     * @return the source
     */
    public Source getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(Source source) {
        this.source = source;
    }

    /**
     * @return the keyPairId
     */
    public UUID getKeyPairId() {
        return keyPairId;
    }

    /**
     * @param keyPairId the keyPairId to set
     */
    public void setKeyPairId(UUID keyPairId) {
        this.keyPairId = keyPairId;
    }

    /**
     * @return the target
     */
    public Target getTarget() {
        return target;
    }

    /**
     * @param target the target to set
     */
    public void setTarget(Target target) {
        this.target = target;
    }
}
