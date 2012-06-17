/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.util.List;

import org.brekka.pegasus.core.dao.DepositDAO;
import org.brekka.pegasus.core.model.Deposit;
import org.brekka.pegasus.core.model.Inbox;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
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
                .add(Restrictions.isNull("deleted"))
                .addOrder(Order.desc("created"))
                .list();
    }
}
