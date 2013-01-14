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
import org.brekka.pegasus.core.dao.TemplateDAO;
import org.brekka.pegasus.core.model.Template;
import org.brekka.pegasus.core.model.Token;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * Hibernate based implementation of {@link TemplateDAO}.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Repository
public class TemplateHibernateDAO extends AbstractPegasusHibernateDAO<Template> implements TemplateDAO {

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.TemplateDAO#retrieveByToken(org.brekka.pegasus.core.model.Token)
     */
    @Override
    public Template retrieveByToken(Token token) {
        return (Template) getCurrentSession().createCriteria(Template.class)
                .add(Restrictions.eq("token", token))
                .uniqueResult();
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.TemplateDAO#retrieveBySlug(java.lang.String)
     */
    @Override
    public Template retrieveBySlug(String slug) {
        return (Template) getCurrentSession().createCriteria(Template.class)
                .add(Restrictions.eq("slug", slug))
                .uniqueResult();
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.TemplateDAO#retrieveListing(org.brekka.commons.persistence.model.ListingCriteria)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Template> retrieveListing(ListingCriteria listingCriteria) {
        Criteria criteria = getCurrentSession().createCriteria(Template.class);
        HibernateUtils.applyCriteria(criteria, listingCriteria);
        return criteria.list();
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.TemplateDAO#retrieveListingRowCount()
     */
    @Override
    public int retrieveListingRowCount() {
        Query query = getCurrentSession().createQuery(
                "select count(t) from Template t");
        return ((Number) query.uniqueResult()).intValue();
    }

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.hibernate.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Template> type() {
        return Template.class;
    }

}
