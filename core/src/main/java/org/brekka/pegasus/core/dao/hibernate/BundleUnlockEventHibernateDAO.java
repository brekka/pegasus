/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.BundleUnlockEventDAO;
import org.brekka.pegasus.core.model.Transfer;
import org.brekka.pegasus.core.model.TransferUnlockEvent;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 * 
 */
@Repository
public class BundleUnlockEventHibernateDAO extends AbstractPegasusHibernateDAO<TransferUnlockEvent> implements
        BundleUnlockEventDAO {

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<TransferUnlockEvent> type() {
        return TransferUnlockEvent.class;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.BundleUnlockEventDAO#retrieveFailedUnlockAttempts(org.brekka.pegasus.core.model.Transfer)
     */
    @Override
    public int retrieveFailedUnlockAttempts(Transfer transfer) {
        Query query = getCurrentSession().createQuery(
                "select count(tue) " +
                "  from TransferUnlockEvent tue" +
                " where tue.transfer=:transfer" +
                "   and tue.success=:success");
        query.setEntity("transfer", transfer);
        query.setBoolean("success", false);
        return ((Number) query.uniqueResult()).intValue();
    }

}
