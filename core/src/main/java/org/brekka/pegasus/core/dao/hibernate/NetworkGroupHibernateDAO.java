/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.NetworkGroupDAO;
import org.brekka.pegasus.core.model.NetworkGroup;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Repository
public class NetworkGroupHibernateDAO extends AbstractPegasusHibernateDAO<NetworkGroup> implements NetworkGroupDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<NetworkGroup> type() {
        return NetworkGroup.class;
    }

}
