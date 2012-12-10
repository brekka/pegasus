/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.DivisionDAO;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.Division;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Repository
public class DivisionHibernateDAO extends AbstractPegasusHibernateDAO<Division<?>> implements DivisionDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected Class type() {
        return Division.class;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.DivisionDAO#retrieveBySlug(org.brekka.pegasus.core.model.Organization, java.lang.String)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public <Owner extends Actor> Division<Owner> retrieveBySlug(Owner owner, String divisionSlug) {
        return (Division) getCurrentSession().createCriteria(Division.class)
                .add(Restrictions.eq("owner", owner))
                .add(Restrictions.eq("slug", divisionSlug))
                .uniqueResult();
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.DivisionDAO#retrieveRootDivision(org.brekka.pegasus.core.model.Organization)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public <Owner extends Actor> Division<Owner> retrieveRootDivision(Owner owner) {
        return (Division) getCurrentSession().createCriteria(Division.class)
                .add(Restrictions.eq("owner", owner))
                .add(Restrictions.isNull("parent"))
                .uniqueResult();
    }
}
