/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.DivisionDAO;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.Organization;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Repository
public class DivisionHibernateDAO extends AbstractPegasusHibernateDAO<Division> implements DivisionDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Division> type() {
        return Division.class;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.DivisionDAO#retrieveBySlug(org.brekka.pegasus.core.model.Organization, java.lang.String)
     */
    @Override
    public Division retrieveBySlug(Organization organization, String divisionSlug) {
        return (Division) getCurrentSession().createCriteria(Division.class)
                .add(Restrictions.eq("organization", organization))
                .add(Restrictions.eq("slug", divisionSlug))
                .uniqueResult();
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.DivisionDAO#retrieveRootDivision(org.brekka.pegasus.core.model.Organization)
     */
    @Override
    public Division retrieveRootDivision(Organization organization) {
        return (Division) getCurrentSession().createCriteria(Division.class)
                .add(Restrictions.eq("organization", organization))
                .add(Restrictions.isNull("parent"))
                .uniqueResult();
    }
}
