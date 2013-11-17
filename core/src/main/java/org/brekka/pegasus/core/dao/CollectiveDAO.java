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

package org.brekka.pegasus.core.dao;

import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.commons.persistence.model.EntityType;
import org.brekka.commons.persistence.model.IdentifiableEntity;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.Collective;
import org.brekka.pegasus.core.model.Member;

/**
 * Manages persistence of {@link Collective} instances
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface CollectiveDAO extends EntityDAO<UUID, Collective> {

    /**
     * @param entity
     * @param entityType
     * @return
     */
    List<Collective> retrieveForEntity(IdentifiableEntity<UUID> entity, EntityType entityType);

    /**
     * @param owner
     * @return
     */
    List<Collective> retrieveByOwner(Actor owner);

    /**
     * @param member
     * @return
     */
    List<Collective> retrieveByConscriptedMember(Member member);

}
