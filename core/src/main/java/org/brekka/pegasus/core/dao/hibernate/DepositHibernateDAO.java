/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.util.Date;
import java.util.List;

import org.brekka.commons.persistence.model.ListingCriteria;
import org.brekka.commons.persistence.support.HibernateUtils;
import org.brekka.pegasus.core.dao.DepositDAO;
import org.brekka.pegasus.core.model.Deposit;
import org.brekka.pegasus.core.model.Inbox;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 * 
 */
@Repository
public class DepositHibernateDAO extends AbstractPegasusHibernateDAO<Deposit> implements DepositDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Deposit> type() {
        return Deposit.class;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.DepositDAO#retrieveByInbox(org.brekka.pegasus.core.model.Inbox)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Deposit> retrieveByInbox(Inbox inbox) {
        return getCurrentSession().createCriteria(Deposit.class)
                .add(Restrictions.eq("inbox", inbox))
                .add(Restrictions.gt("expires", new Date()))
                .add(Restrictions.isNull("deleted"))
                .addOrder(Order.desc("created"))
                .list();
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.DepositDAO#retrieveListing(org.brekka.pegasus.core.model.Inbox, org.joda.time.DateTime, org.joda.time.DateTime, boolean, org.brekka.commons.persistence.model.ListingCriteria)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Deposit> retrieveListing(Inbox inbox, DateTime from, DateTime until, boolean showExpired,
            ListingCriteria listingCriteria) {
        Criteria criteria = getCurrentSession().createCriteria(Deposit.class);
        criteria.add(Restrictions.eq("inbox", inbox));
        criteria.add(Restrictions.gt("created", from.toDate()));
        criteria.add(Restrictions.lt("created", until.toDate()));
        if (!showExpired) {
            criteria.add(Restrictions.gt("expires", new Date()));
        }
        HibernateUtils.applyCriteria(criteria, listingCriteria);
        return criteria.list();
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.DepositDAO#retrieveListingRowCount(org.brekka.pegasus.core.model.Inbox, org.joda.time.DateTime, org.joda.time.DateTime, boolean)
     */
    @Override
    public int retrieveListingRowCount(Inbox inbox, DateTime from, DateTime until, boolean showExpired) {
        String hql = 
                "select count(d) from Deposit d" +
                " where d.inbox=:inbox" +
                "   and d.created>:from" +
                "   and d.created<:until";
        if (!showExpired) {
            hql += " and d.expires>:now";
        }
        Query query = getCurrentSession().createQuery(hql);
        query.setEntity("inbox", inbox);
        query.setDate("from", from.toDate());
        query.setDate("until", until.toDate());
        if (!showExpired) {
            hql += " and d.expires>:now";
            query.setDate("now", new Date());
        }
        return ((Number) query.uniqueResult()).intValue();
    }
}
