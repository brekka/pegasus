/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.util.List;

import org.brekka.pegasus.core.dao.NetworkDAO;
import org.brekka.pegasus.core.model.Network;
import org.brekka.pegasus.core.model.NetworkGroup;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Repository
public class NetworkHibernateDAO extends AbstractPegasusHibernateDAO<Network> implements NetworkDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Network> type() {
        return Network.class;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.NetworkDAO#retrieveForGroup(org.brekka.pegasus.core.model.NetworkGroup)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Network> retrieveForGroup(NetworkGroup group) {
        return getCurrentSession().createCriteria(Network.class)
                .add(Restrictions.eq("networkGroup", group))
                .list();
    }
}
