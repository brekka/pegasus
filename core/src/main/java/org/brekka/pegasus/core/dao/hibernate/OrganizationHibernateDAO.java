/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.OrganizationDAO;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Token;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Repository
public class OrganizationHibernateDAO extends AbstractPegasusHibernateDAO<Organization> implements OrganizationDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Organization> type() {
        return Organization.class;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.OrganizationDAO#retrieveByToken(java.lang.String)
     */
    @Override
    public Organization retrieveByToken(Token token) {
        return (Organization) getCurrentSession().createCriteria(Organization.class)
                .add(Restrictions.eq("token", token))
                .uniqueResult();
    }
}
