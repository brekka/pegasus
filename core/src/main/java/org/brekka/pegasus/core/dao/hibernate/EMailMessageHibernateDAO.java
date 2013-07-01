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

import org.brekka.commons.persistence.model.ListingCriteria;
import org.brekka.commons.persistence.support.HibernateUtils;
import org.brekka.pegasus.core.dao.EMailMessageDAO;
import org.brekka.pegasus.core.model.EMailAddress;
import org.brekka.pegasus.core.model.EMailMessage;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * EMail Message HibernateDAO
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Repository
public class EMailMessageHibernateDAO extends AbstractPegasusHibernateDAO<EMailMessage> implements EMailMessageDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.hibernate.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<EMailMessage> type() {
        return EMailMessage.class;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.EMailMessageDAO#retrieveForRecipient(org.brekka.pegasus.core.model.EMailAddress, org.brekka.commons.persistence.model.ListingCriteria)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<EMailMessage> retrieveForRecipient(EMailAddress eMailAddress, ListingCriteria listingCriteria) {
        Criteria criteria = getCurrentSession().createCriteria(EMailMessage.class)
            .createCriteria("recipients")
            .add(Restrictions.eq("address", eMailAddress));
        HibernateUtils.applyCriteria(criteria, listingCriteria);
        return criteria.list();
    }
    
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.TemplateDAO#retrieveListingRowCount()
     */
    @Override
    public int retrieveForRecipientRowCount(EMailAddress eMailAddress) {
        Query query = getCurrentSession().createQuery(
                "select count(em) from EMailMessage em" +
                "  join em.recipients as rec" +
                " where rec.address = :address");
        query.setParameter("address", eMailAddress);
        return ((Number) query.uniqueResult()).intValue();
    }
}
