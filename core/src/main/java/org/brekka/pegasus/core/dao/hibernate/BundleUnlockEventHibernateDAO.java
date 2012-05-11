/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.BundleUnlockEventDAO;
import org.brekka.pegasus.core.model.BundleUnlockEvent;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 * 
 */
@Repository
public class BundleUnlockEventHibernateDAO extends AbstractPegasusHibernateDAO<BundleUnlockEvent> implements
        BundleUnlockEventDAO {

    /*
     * (non-Javadoc)
     * 
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<BundleUnlockEvent> type() {
        return BundleUnlockEvent.class;
    }

}
