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
import org.brekka.pegasus.core.dao.ParticipantDAO;
import org.brekka.pegasus.core.model.Collective;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Participant;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * Hibernate base implementation of the {@link ParticipantDAO}.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Repository
public class ParticipantHibernateDAO extends AbstractPegasusHibernateDAO<Participant> implements ParticipantDAO {

    @Override
    protected Class<Participant> type() {
        return Participant.class;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.ParticipantDAO#retrieveByMember(org.brekka.pegasus.core.model.Collective, org.brekka.pegasus.core.model.Member)
     */
    @Override
    public Participant retrieveByMember(final Collective collective, final Member member) {
        return (Participant) getCurrentSession().createCriteria(Participant.class)
                .add(Restrictions.eq("collective", collective))
                .add(Restrictions.eq("member", member))
                .uniqueResult();
    }
}
