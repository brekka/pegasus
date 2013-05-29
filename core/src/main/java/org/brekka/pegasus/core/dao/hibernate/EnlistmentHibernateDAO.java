/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.util.List;

import org.brekka.pegasus.core.dao.EnlistmentDAO;
import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.Enlistment;
import org.brekka.pegasus.core.model.Organization;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Repository
public class EnlistmentHibernateDAO extends AbstractPegasusHibernateDAO<Enlistment> implements EnlistmentDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Enlistment> type() {
        return Enlistment.class;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.EnlistmentDAO#retrieveForOrg(org.brekka.pegasus.core.model.Organization, org.brekka.pegasus.core.model.Associate)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Enlistment> retrieveForAssociate(Associate associate) {
        return getCurrentSession().createCriteria(Enlistment.class)
                .add(Restrictions.eq("owner", associate))
                .list();
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.EnlistmentDAO#retrieveEnlistmentByTarget(org.brekka.pegasus.core.model.Division, org.brekka.pegasus.core.model.Member)
     */
    @Override
    public Enlistment retrieveEnlistmentByTarget(Division<Organization> target, Associate associate) {
        return (Enlistment) getCurrentSession().createCriteria(Enlistment.class)
                .add(Restrictions.eq("target", target))
                .add(Restrictions.eq("owner", associate))
                .uniqueResult();
    }
}
