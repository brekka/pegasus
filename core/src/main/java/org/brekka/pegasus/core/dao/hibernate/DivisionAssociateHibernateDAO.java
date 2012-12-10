/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.util.List;

import org.brekka.pegasus.core.dao.DivisionAssociateDAO;
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
public class DivisionAssociateHibernateDAO extends AbstractPegasusHibernateDAO<Enlistment> implements DivisionAssociateDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Enlistment> type() {
        return Enlistment.class;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.DivisionAssociateDAO#retrieveBySurrogateKey(org.brekka.pegasus.core.model.Division, org.brekka.pegasus.core.model.Associate)
     */
    @Override
    public Enlistment retrieveBySurrogateKey(Division<Organization> division, Associate associate) {
        return (Enlistment) getCurrentSession().createCriteria(Enlistment.class)
                .add(Restrictions.eq("division", division))
                .add(Restrictions.eq("associate", associate))
                .uniqueResult();
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.DivisionAssociateDAO#retrieveForOrg(org.brekka.pegasus.core.model.Organization, org.brekka.pegasus.core.model.Associate)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Enlistment> retrieveForOrg(Associate associate) {
        return getCurrentSession().createCriteria(Enlistment.class)
                .add(Restrictions.eq("associate", associate))
                .list();
    }
}
