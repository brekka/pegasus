/**
 *
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.model.ListingCriteria;
import org.brekka.commons.persistence.support.FirstResultTransformer;
import org.brekka.commons.persistence.support.HibernateUtils;
import org.brekka.pegasus.core.dao.DepositDAO;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.AllocationDisposition;
import org.brekka.pegasus.core.model.Deposit;
import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.core.model.Member;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.LogicalExpression;
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
    public List<Deposit> retrieveByInbox(final Inbox inbox) {
        return getCurrentSession().createCriteria(Deposit.class)
                .add(Restrictions.eq("inbox", inbox))
                .add(Restrictions.gt("expires", new Date()))
                .add(Restrictions.isNull("deleted"))
                .addOrder(Order.desc("created"))
                .list();
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.DepositDAO#retrieveById(java.util.UUID, org.brekka.pegasus.core.model.Member)
     */
    @Override
    public Deposit retrieveById(final UUID depositId, final Member memberCanAccess) {
        String hql = "select d "
                   + "  from Participant as p "
                   + "  join p.collective as c "
                   + "  join c.inbox as i "
                   + "  join i.deposits as d "
                   + " where p.member=:member "
                   + "   and d.id=:depositId ";
        Query q = getCurrentSession().createQuery(hql);
        q.setParameter("depositId", depositId);
        q.setParameter("member", memberCanAccess);
        return (Deposit) q.uniqueResult();
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.DepositDAO#retrieveListing(org.brekka.pegasus.core.model.Inbox, org.joda.time.DateTime, org.joda.time.DateTime, boolean, org.brekka.commons.persistence.model.ListingCriteria)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Deposit> retrieveListing(final Inbox inbox, final DateTime from, final DateTime until, final boolean showExpired,
            final ListingCriteria listingCriteria, final boolean dispatchBased, final List<? extends Actor> sentByActors) {
        Criteria criteria = getCurrentSession().createCriteria(Deposit.class);
        criteria.add(Restrictions.eq("inbox", inbox));
        criteria.add(Restrictions.gt("created", from.toDate()));
        criteria.add(Restrictions.lt("created", until.toDate()));
        if (sentByActors != null
                && !sentByActors.isEmpty()) {
            criteria.add(Restrictions.in("actor", sentByActors));
        }

        LogicalExpression expiresGtOrNull = Restrictions.or(Restrictions.gt("expires", new Date()), Restrictions.isNull("expires"));
        if (dispatchBased) {
            Criteria joinCriteria = criteria.createCriteria("derivedFrom", JoinType.LEFT_OUTER_JOIN);
            joinCriteria.add(expiresGtOrNull);
        } else if (!showExpired) {
            criteria.add(expiresGtOrNull);
        }
        HibernateUtils.applyCriteria(criteria, listingCriteria);
        List<Deposit> list = criteria.list();
        return list;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.DepositDAO#retrieveListingRowCount(org.brekka.pegasus.core.model.Inbox, org.joda.time.DateTime, org.joda.time.DateTime, boolean)
     */
    @Override
    public int retrieveListingRowCount(final Inbox inbox, final DateTime from, final DateTime until, final boolean showExpired, final boolean dispatchBased) {
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

    /*
     * (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.DepositDAO#retrieveDepositsForParticipant(org.brekka.pegasus.core.model.Member, org.brekka.pegasus.core.model.AllocationDisposition)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Deposit> retrieveDepositsForParticipant(final Member member, final AllocationDisposition allocationDisposition, final boolean personalOnly,
            final boolean includeExpired) {
        String hql = "select d, df "
                  + "  from Participant p "
                  + "  join p.collective as c "
                  + "  join c.inbox as i "
                  + "  join i.deposits as d "
                  + "  join d.derivedFrom as df "
                  + " where p.member=:member "
                  + "   and df.deleted is null ";
        if  (allocationDisposition != null) {
            hql +=  "   and d.disposition=:disposition";
        }
        if (personalOnly) {
            hql +=  "   and c.personal=:personal";
        }
        if (!includeExpired) {
            hql +=   "  and (d.expires is null or d.expires>:now)";
        }
        hql +=      " order by d.created desc";
        Query q = getCurrentSession().createQuery(hql)
              .setParameter("member", member);
        if (allocationDisposition != null) {
            q.setParameter("disposition", allocationDisposition);
        }
        if (personalOnly) {
            q.setParameter("personal", personalOnly);
        }
        if (!includeExpired) {
            q.setTimestamp("now", new Date());
        }
        q.setResultTransformer(FirstResultTransformer.INSTANCE);
        return q.list();
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.DepositDAO#retrieveDepositsForCollectiveOwner(org.brekka.pegasus.core.model.Actor, org.brekka.pegasus.core.model.AllocationDisposition)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Deposit> retrieveDepositsForCollectiveOwner(final Actor owner, final AllocationDisposition allocationDisposition,
            final boolean includePersonal, final boolean includeExpired) {
        String hql = "select d, df "
                   + "  from Collective c "
                   + "  join c.inbox as i "
                   + "  join i.deposits as d "
                   + "  join d.derivedFrom as df "
                   + " where c.owner=:owner "
                   + "   and df.deleted is null "
                   + "   and c.personal=:personal ";
        if  (allocationDisposition != null) {
            hql +=   "   and d.disposition=:disposition";
        }
        if (!includeExpired) {
            hql +=   "  and d.expires>:now";
        }
        hql +=       " order by d.created desc";
        Query q = getCurrentSession().createQuery(hql)
              .setParameter("owner", owner);
        if (allocationDisposition != null) {
            q.setParameter("disposition", allocationDisposition);
        }
        q.setParameter("personal", includePersonal);
        if (!includeExpired) {
            q.setTimestamp("now", new Date());
        }
        q.setResultTransformer(FirstResultTransformer.INSTANCE);
        return q.list();
    }
}
