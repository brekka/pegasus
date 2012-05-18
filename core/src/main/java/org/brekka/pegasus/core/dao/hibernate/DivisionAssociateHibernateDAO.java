/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.util.List;

import org.brekka.pegasus.core.dao.DivisionAssociateDAO;
import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.DivisionAssociate;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Repository
public class DivisionAssociateHibernateDAO extends AbstractPegasusHibernateDAO<DivisionAssociate> implements DivisionAssociateDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<DivisionAssociate> type() {
        return DivisionAssociate.class;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.DivisionAssociateDAO#retrieveBySurrogateKey(org.brekka.pegasus.core.model.Division, org.brekka.pegasus.core.model.Associate)
     */
    @Override
    public DivisionAssociate retrieveBySurrogateKey(Division division, Associate associate) {
        return (DivisionAssociate) getCurrentSession().createCriteria(DivisionAssociate.class)
                .add(Restrictions.eq("division", division))
                .add(Restrictions.eq("associate", associate))
                .uniqueResult();
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.DivisionAssociateDAO#retrieveForOrg(org.brekka.pegasus.core.model.Organization, org.brekka.pegasus.core.model.Associate)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<DivisionAssociate> retrieveForOrg(Associate associate) {
        return getCurrentSession().createCriteria(DivisionAssociate.class)
                .add(Restrictions.eq("associate", associate))
                .list();
    }
}
