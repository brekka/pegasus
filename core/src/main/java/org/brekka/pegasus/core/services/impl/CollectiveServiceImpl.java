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

package org.brekka.pegasus.core.services.impl;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.ObjectUtils;
import org.brekka.commons.persistence.model.EntityType;
import org.brekka.commons.persistence.model.IdentifiableEntity;
import org.brekka.commons.persistence.support.EntityUtils;
import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.dao.AssignmentDAO;
import org.brekka.pegasus.core.dao.CollectiveDAO;
import org.brekka.pegasus.core.dao.ParticipantDAO;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.Assignment;
import org.brekka.pegasus.core.model.Collective;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Participant;
import org.brekka.pegasus.core.model.Partnership;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.CollectiveService;
import org.brekka.pegasus.core.services.DivisionService;
import org.brekka.pegasus.core.utils.UuidUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * TODO Description of CollectiveServiceImpl
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Service
public class CollectiveServiceImpl implements CollectiveService {

    @Autowired
    private CollectiveDAO collectiveDAO;

    @Autowired
    private ParticipantDAO participantDAO;

    @Autowired
    private AssignmentDAO assignmentDAO;

    @Autowired
    private DivisionService divisionService;


    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.CollectiveService#create(org.brekka.pegasus.core.model.Collective)
     */
    @Override
    @Transactional
    public UUID create(final Collective collective) {
        return this.collectiveDAO.create(collective);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.CollectiveService#delete(java.util.UUID)
     */
    @Override
    @Transactional
    public void delete(final Collective collective) {
        this.collectiveDAO.delete(collective.getId());
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.CollectiveService#update(org.brekka.pegasus.core.model.Collective)
     */
    @Override
    @Transactional
    public Collective update(final Collective collective) {
        Collective managed = this.collectiveDAO.retrieveById(collective.getId());
        managed.setDescription(collective.getDescription());
        managed.setName(collective.getName());
        managed.setKey(collective.getKey());
        this.collectiveDAO.update(managed);
        return managed;
    }


    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.CollectiveService#retrieveForOwner(org.brekka.pegasus.core.model.Actor)
     */
    @Override
    @Transactional(readOnly=true)
    public List<Collective> retrieveForOwner(final Actor owner, final boolean includePersonal) {
        return this.collectiveDAO.retrieveByOwner(owner, includePersonal);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.CollectiveService#retrieveMemberOf(org.brekka.pegasus.core.model.Member)
     */
    @Override
    @Transactional(readOnly=true)
    public List<Collective> retrieveMemberOf(final Member member) {
        return this.collectiveDAO.retrieveByConscriptedMember(member);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.CollectiveService#retrieveById(java.util.UUID)
     */
    @Override
    @Transactional(readOnly=true)
    public Collective retrieveById(final UUID collectiveId) {
        return this.collectiveDAO.retrieveById(collectiveId);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.CollectiveService#retrieveByKey(org.brekka.pegasus.core.model.Actor, java.lang.String)
     */
    @Override
    @Transactional(readOnly=true)
    public Collective retrieveByKey(final Actor owner, final String key) {
        return this.collectiveDAO.retrieveByKey(owner, key);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.CollectiveService#retrieveConscriptById(java.util.UUID)
     */
    @Override
    @Transactional(readOnly=true)
    public Participant retrieveParticipantById(final UUID conscriptId) {
        return this.participantDAO.retrieveById(conscriptId);
    }


    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.CollectiveService#enlist(org.brekka.pegasus.core.model.Collective, org.brekka.pegasus.core.model.Member)
     */
    @Override
    @Transactional
    public UUID enlist(final Collective collective, final Member member) {
        Participant participant = new Participant();
        participant.setMember(member);
        participant.setCollective(collective);
        if (collective.getInbox() != null) {
            Inbox inbox = collective.getInbox();
            KeySafe<?> inboxKeySafe = EntityUtils.narrow(inbox.getKeySafe(), KeySafe.class);
            KeySafe<?> memberKeySafe = EntityUtils.narrow(member.getPrimaryKeySafe(), KeySafe.class);
            if (ObjectUtils.equals(inboxKeySafe, memberKeySafe)) {
                // No partnership required, keys are the same.
            } else if (inboxKeySafe instanceof Division
                    && memberKeySafe instanceof Division) {
                Division<Member> memberDivision = (Division) memberKeySafe;
                Division<Actor> inboxDivision = (Division) inboxKeySafe;
                Partnership<Member, Actor> partnership = this.divisionService.createPartnership(member, memberDivision, inboxDivision);
                participant.setPartnership(partnership);
            } else if (inboxKeySafe instanceof Vault) {
                // Inbox is using a vault, no additional keys required
            } else {
                throw new PegasusException(PegasusErrorCode.PG105, "Unable to enlist member '%s' to collective '%s'. inbox/member key safes are not divisions, are instead"
                        + " %s/%s", member.getId(), collective.getId(), inboxKeySafe.getClass().getName(), memberKeySafe.getClass().getName());
            }
        }
        UUID conscriptId = this.participantDAO.create(participant);
        return conscriptId;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.CollectiveService#discharge(org.brekka.pegasus.core.model.Conscript)
     */
    @Override
    @Transactional
    public void discharge(final Participant conscript) {
        this.participantDAO.delete(conscript.getId());
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.CollectiveService#discharge(org.brekka.pegasus.core.model.Collective, org.brekka.pegasus.core.model.Member)
     */
    @Override
    @Transactional
    public void discharge(final Collective collective, final Member member) {
        Participant participant = this.participantDAO.retrieveByMember(collective, member);
        this.participantDAO.delete(participant.getId());
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.CollectiveService#assign(org.brekka.commons.persistence.model.IdentifiableEntity, org.brekka.commons.persistence.model.EntityType, org.brekka.pegasus.core.model.Collective[])
     */
    @Override
    @Transactional
    public void assign(final IdentifiableEntity<UUID> entity, final EntityType entityType, final Collective... too) {
        for (Collective collective : too) {
            Assignment assignment = new Assignment();
            assignment.setEntityId(UuidUtils.toBytes(entity.getId()));
            assignment.setEntityType(entityType);
            assignment.setCollective(collective);
            this.assignmentDAO.create(assignment);
        }
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.CollectiveService#retrieveAssignments(org.brekka.commons.persistence.model.IdentifiableEntity, org.brekka.commons.persistence.model.EntityType)
     */
    @Override
    @Transactional(readOnly=true)
    public List<Collective> retrieveAssignments(final IdentifiableEntity<UUID> entity, final EntityType entityType) {
        return this.collectiveDAO.retrieveForEntity(entity, entityType);
    }


}
