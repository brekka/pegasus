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

package org.brekka.pegasus.core.services;

import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.model.EntityType;
import org.brekka.commons.persistence.model.IdentifiableEntity;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.Collective;
import org.brekka.pegasus.core.model.Participant;
import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Member;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface CollectiveService {

    /**
     * {@link Collective}s can be owned by any actor. Retrieve the list of collectives
     * owned by the specified actor.
     *
     * @param owner
     * @return
     */
    List<Collective> retrieveForOwner(Actor owner);

    /**
     * Retrieve all collectives for the specified member.
     *
     * @param member
     * @return
     */
    List<Collective> retrieveMemberOf(Member member);

    Collective retrieveById(UUID collectiveId);

    Participant retrieveParticipantById(UUID conscriptId);

    UUID create(Collective collective);

    void delete(Collective collective);

    void update(Collective collective);

    /**
     * Create a new {@link Participant} and return its ID.
     * 
     * @param collective
     * @param member
     * @return
     */
    UUID enlist(Collective collective, Member member);

    void discharge(Participant conscript);

    /**
     * Assign the specified collectives to the given entity.
     * @param entity
     * @param entityType
     * @param too
     */
    void assign(IdentifiableEntity<UUID> entity, EntityType entityType, Collective... too);
    
    /**
     * Retrieve the list of collectives that were assigned to the specified entity.
     * @param entity
     * @param entityType
     * @return
     */
    List<Collective> retrieveAssignments(IdentifiableEntity<UUID> entity, EntityType entityType);
}
