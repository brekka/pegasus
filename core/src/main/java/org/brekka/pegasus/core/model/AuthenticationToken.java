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
import javax.persistence.Table;

import org.brekka.commons.persistence.model.LongevousEntity;
import org.brekka.pegasus.core.PegasusConstants;
import org.hibernate.annotations.Type;

/**
 * Authentication Token used to identify a person.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`AuthenticationToken`", schema=PegasusConstants.SCHEMA)
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
        name="`Type`", length=12,
        discriminatorType=DiscriminatorType.STRING
        )
@DiscriminatorValue("Base")
public abstract class AuthenticationToken extends LongevousEntity<UUID> {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -7223318842397626756L;

    /**
     * Unique id
     */
    @Id
    @Type(type="pg-uuid")
    @Column(name="`ID`")
    private UUID id;

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
     * Retrieve the user name
     * @return
     */
    public abstract String getUsername();
}
