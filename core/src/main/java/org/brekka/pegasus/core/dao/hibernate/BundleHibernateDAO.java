/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.BundleDAO;
import org.brekka.pegasus.core.model.Bundle;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor
 *
 */
@Repository
public class BundleHibernateDAO extends AbstractPegasusHibernateDAO<Bundle> implements BundleDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Bundle> type() {
        return Bundle.class;
    }

}
