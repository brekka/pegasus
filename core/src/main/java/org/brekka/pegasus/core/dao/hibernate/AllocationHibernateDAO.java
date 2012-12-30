/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.sql.Date;
import java.util.List;

import org.brekka.commons.persistence.model.ListingCriteria;
import org.brekka.commons.persistence.support.HibernateUtils;
import org.brekka.pegasus.core.dao.AllocationDAO;
import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.model.Dispatch;
import org.hibernate.Criteria;
import org.hibernate.Query;
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
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.AllocationDAO#retrieveDerivedFromListing(org.brekka.pegasus.core.model.Dispatch, org.brekka.commons.persistence.model.ListingCriteria)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Allocation> retrieveDerivedFromListing(Dispatch derivedFrom, ListingCriteria listingCriteria) {
        Criteria criteria = getCurrentSession().createCriteria(Allocation.class);
        criteria.add(Restrictions.eq("derivedFrom", derivedFrom));
        HibernateUtils.applyCriteria(criteria, listingCriteria);
        return criteria.list();
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.AllocationDAO#retrieveDerivedFromListingRowCount(org.brekka.pegasus.core.model.Dispatch)
     */
    @Override
    public int retrieveDerivedFromListingRowCount(Dispatch derivedFrom) {
        Query query = getCurrentSession().createQuery(
                "select count(a) from Allocation a" +
                " where a.derivedFrom=:derivedFrom");
        query.setEntity("derivedFrom", derivedFrom);
        return ((Number) query.uniqueResult()).intValue();
    }
}
