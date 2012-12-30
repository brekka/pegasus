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

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.hibernate.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Robot> type() {
        return Robot.class;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.RobotDAO#retrieveRobotListing(org.brekka.pegasus.core.model.Actor, org.brekka.commons.persistence.model.ListingCriteria)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Robot> retrieveListing(Actor owner, ListingCriteria listingCriteria) {
        Criteria criteria = getCurrentSession().createCriteria(Robot.class);
        criteria.add(Restrictions.eq("owner", owner));
        HibernateUtils.applyCriteria(criteria, listingCriteria);
        return criteria.list();
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.RobotDAO#retrieveRobotListingRowCount(org.brekka.pegasus.core.model.Actor)
     */
    @Override
    public int retrieveListingRowCount(Actor owner) {
        Query query = getCurrentSession().createQuery(
                "select count(r) from Robot r" +
                " where r.owner=:owner");
        query.setEntity("owner", owner);
        return ((Number) query.uniqueResult()).intValue();
    }

}
