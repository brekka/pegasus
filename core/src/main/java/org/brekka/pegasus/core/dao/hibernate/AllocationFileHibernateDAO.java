/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.sql.Date;
import java.util.List;

import org.brekka.pegasus.core.dao.AllocationFileDAO;
import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.model.AllocationFile;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Repository
public class AllocationFileHibernateDAO extends AbstractPegasusHibernateDAO<AllocationFile> implements AllocationFileDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<AllocationFile> type() {
        return AllocationFile.class;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.AllocationFileDAO#retrieveByBundle(org.brekka.pegasus.core.model.Bundle)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<AllocationFile> retrieveByAllocation(Allocation allocation) {
        return getCurrentSession().createCriteria(AllocationFile.class)
                .add(Restrictions.eq("allocation", allocation))
                .list();
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.AllocationFileDAO#retrieveOldestExpired(int)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<AllocationFile> retrieveOldestExpired(int maxFileCount) {
        return getCurrentSession().createCriteria(AllocationFile.class)
                .add(Restrictions.isNull("deleted"))
                .add(Restrictions.lt("expires", new Date(System.currentTimeMillis())))
                .setMaxResults(maxFileCount)
                .addOrder(Order.asc("expires"))
                .list();
    }
}
