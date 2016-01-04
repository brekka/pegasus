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

package org.brekka.pegasus.core.dao.hibernate;

import java.util.List;

import org.brekka.commons.persistence.model.ListingCriteria;
import org.brekka.commons.persistence.support.HibernateUtils;
import org.brekka.pegasus.core.dao.RobotDAO;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.ActorStatus;
import org.brekka.pegasus.core.model.Robot;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * Robot Hibernate DAO
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Repository
public class RobotHibernateDAO extends AbstractPegasusHibernateDAO<Robot> implements RobotDAO {

    @Override
    protected Class<Robot> type() {
        return Robot.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Robot> retrieveListing(final Actor owner, final ListingCriteria listingCriteria) {
        Criteria criteria = getCurrentSession().createCriteria(Robot.class);
        if (owner != null) {
            criteria.add(Restrictions.eq("owner", owner));
        }
        criteria.add(Restrictions.in("status", new Object[] { ActorStatus.ACTIVE, ActorStatus.DISABLED } ));
        HibernateUtils.applyCriteria(criteria, listingCriteria);
        return criteria.list();
    }

    @Override
    public int retrieveListingRowCount(final Actor owner) {
        String sql =
                "select count(r) from Robot r" +
                " where r.status in (:active, :disabled)";
        if (owner != null) {
            sql += "   and r.owner=:owner";
        }
        Query query = getCurrentSession().createQuery(sql);
        if (owner != null) {
            query.setEntity("owner", owner);
        }
        query.setParameter("active", ActorStatus.ACTIVE);
        query.setParameter("disabled", ActorStatus.DISABLED);
        return ((Number) query.uniqueResult()).intValue();
    }
}
