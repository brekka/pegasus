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
import org.hibernate.sql.JoinType;
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
            ListingCriteria listingCriteria, boolean dispatchBased) {
        Criteria criteria = getCurrentSession().createCriteria(Deposit.class);
        criteria.add(Restrictions.eq("inbox", inbox));
        criteria.add(Restrictions.gt("created", from.toDate()));
        criteria.add(Restrictions.lt("created", until.toDate()));
        if (dispatchBased) {
            Criteria joinCriteria = criteria.createCriteria("derivedFrom", JoinType.LEFT_OUTER_JOIN);
            joinCriteria.add(Restrictions.gt("expires", new Date()));
        } else if (!showExpired) {
            criteria.add(Restrictions.gt("expires", new Date()));
        }
        HibernateUtils.applyCriteria(criteria, listingCriteria);
        List<Deposit> list = criteria.list();
        return list;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.DepositDAO#retrieveListingRowCount(org.brekka.pegasus.core.model.Inbox, org.joda.time.DateTime, org.joda.time.DateTime, boolean)
     */
    @Override
    public int retrieveListingRowCount(Inbox inbox, DateTime from, DateTime until, boolean showExpired, boolean dispatchBased) {
        String hql = "select count(d) from Deposit d";
        if (dispatchBased) {
            hql += "  left join d.derivedFrom as disp";
        }
        hql +=  " where d.inbox=:inbox" +
                "   and d.created>:from" +
                "   and d.created<:until";
        if (dispatchBased) {
            hql += "  and disp.expires>:now";
        } else if (!showExpired) {
            hql += "  and d.expires>:now";
        }
        
        Query query = getCurrentSession().createQuery(hql);
        query.setEntity("inbox", inbox);
        query.setTimestamp("from", from.toDate());
        query.setTimestamp("until", until.toDate());
        if (dispatchBased 
                || !showExpired) {
            query.setTimestamp("now", new Date());
        }
        int count = ((Number) query.uniqueResult()).intValue();
        return count;
    }
}
