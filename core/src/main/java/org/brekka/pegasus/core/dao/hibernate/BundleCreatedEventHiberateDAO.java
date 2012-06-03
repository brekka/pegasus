/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.BundleCreatedEventDAO;
import org.brekka.pegasus.core.model.TransferCreatedEvent;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Repository
public class BundleCreatedEventHiberateDAO extends AbstractPegasusHibernateDAO<TransferCreatedEvent> implements
        BundleCreatedEventDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<TransferCreatedEvent> type() {
        return TransferCreatedEvent.class;
    }

}
