/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.sql.Date;
import java.util.List;

import org.brekka.pegasus.core.dao.AllocationDAO;
import org.brekka.pegasus.core.model.Allocation;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Repository
public class AllocationHibernateDAO extends AbstractPegasusHibernateDAO<Allocation> implements AllocationDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Allocation> type() {
        return Allocation.class;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.AllocationDAO#refresh(org.brekka.pegasus.core.model.Allocation)
     */
    @Override
    public void refresh(Allocation allocation) {
        getCurrentSession().refresh(allocation);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.AllocationDAO#retrieveOldestExpired(int)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Allocation> retrieveOldestExpired(int maxAllocationCount) {
        return getCurrentSession().createCriteria(Allocation.class)
                .add(Restrictions.isNull("deleted"))
                .add(Restrictions.lt("expires", new Date(System.currentTimeMillis())))
                .setMaxResults(maxAllocationCount)
                .addOrder(Order.asc("expires"))
                .list();
    }
}
