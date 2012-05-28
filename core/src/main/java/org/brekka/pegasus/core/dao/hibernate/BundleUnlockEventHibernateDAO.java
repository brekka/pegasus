/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.BundleUnlockEventDAO;
import org.brekka.pegasus.core.model.TransferUnlockEvent;
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

}
