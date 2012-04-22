/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.BundleCreatedEventDAO;
import org.brekka.pegasus.core.model.BundleCreatedEvent;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor
 *
 */
@Repository
public class BundleCreatedEventHiberateDAO extends AbstractPegasusHibernateDAO<BundleCreatedEvent> implements
        BundleCreatedEventDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<BundleCreatedEvent> type() {
        return BundleCreatedEvent.class;
    }

}
