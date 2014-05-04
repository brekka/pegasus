/**
 *
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.util.Date;
import java.util.List;

import org.brekka.pegasus.core.dao.DispatchDAO;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.AllocationDisposition;
import org.brekka.pegasus.core.model.Dispatch;
import org.brekka.pegasus.core.model.KeySafe;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Repository
public class DispatchHibernateDAO extends AbstractPegasusHibernateDAO<Dispatch> implements DispatchDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Dispatch> type() {
        return Dispatch.class;
    }


    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.DispatchDAO#retrieveForInterval(org.brekka.pegasus.core.model.KeySafe, org.brekka.pegasus.core.model.Actor, java.util.Date, java.util.Date)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Dispatch> retrieveForInterval(final KeySafe<?> keySafe, final Actor actor, final Date from, final Date until) {
        return getCurrentSession().createCriteria(Dispatch.class)
                .add(Restrictions.eq("keySafe", keySafe))
                .add(Restrictions.eq("actor", actor))
                .add(Restrictions.gt("created", from))
                .add(Restrictions.lt("created", until))
                .list();
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.DispatchDAO#find(org.brekka.pegasus.core.model.KeySafe, org.brekka.pegasus.core.model.AllocationDisposition)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Dispatch> find(final KeySafe<?> keySafe, final AllocationDisposition disposition) {
        return getCurrentSession().createCriteria(Dispatch.class)
                .add(Restrictions.eq("keySafe", keySafe))
                .add(Restrictions.eq("disposition", disposition))
                .addOrder(Order.desc("created"))
                .list();
    }
}
