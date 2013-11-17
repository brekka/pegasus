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

package org.brekka.pegasus.core.dao.hibernate;

import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.model.EntityType;
import org.brekka.commons.persistence.model.IdentifiableEntity;
import org.brekka.pegasus.core.dao.CollectiveDAO;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.Collective;
import org.brekka.pegasus.core.model.Member;
import org.springframework.stereotype.Repository;

/**
 * Hibernate base implementation of the {@link CollectiveDAO}.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Repository
public class CollectiveHibernateDAO extends AbstractPegasusHibernateDAO<Collective> implements CollectiveDAO {

    @Override
    protected Class<Collective> type() {
        return Collective.class;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.AssignmentDAO#retrieveForEntity(org.brekka.commons.persistence.model.IdentifiableEntity, org.brekka.commons.persistence.model.EntityType)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Collective> retrieveForEntity(final IdentifiableEntity<UUID> entity, final EntityType entityType) {
        return getCurrentSession().createQuery(
                  "select c "
                + "  from Assignment a "
                + "  join a.collective as c"
                + " where a.entityId=:entityId"
                + "   and a.entityType=:entityType"
                + " order by c.name asc")
                .setParameter("entityId", entity.getId())
                .setParameter("entityType", entityType)
                .list();
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.CollectiveDAO#retrieveByOwner(org.brekka.pegasus.core.model.Actor)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Collective> retrieveByOwner(final Actor owner) {
        return getCurrentSession().createQuery(
                "select c "
                + "from Collective c "
              + " where c.owner=:owner "
              + " order by c.name asc")
              .setParameter("owner", owner)
              .list();
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.CollectiveDAO#retrieveByConscriptedMember(org.brekka.pegasus.core.model.Member)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Collective> retrieveByConscriptedMember(final Member member) {
        return getCurrentSession().createQuery(
                "select c "
              + "  from Participant p "
              + "  join p.collective as c "
              + " where p.member=:member "
              + " order by c.name asc")
              .setParameter("member", member)
              .list();
    }


}