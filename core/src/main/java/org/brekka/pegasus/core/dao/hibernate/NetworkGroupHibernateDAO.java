/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.util.List;

import org.brekka.pegasus.core.dao.NetworkGroupDAO;
import org.brekka.pegasus.core.model.Firewall;
import org.brekka.pegasus.core.model.Network;
import org.brekka.pegasus.core.model.NetworkGroup;
import org.hibernate.criterion.Restrictions;
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

    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.NetworkGroupDAO#retrieveCategorized()
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<NetworkGroup> retrieveCategorized(Firewall excludeFrom) {
        if (excludeFrom == null) {
            return getCurrentSession().createCriteria(NetworkGroup.class)
                    .add(Restrictions.isNotNull("networkGroupCategory"))
                    .list();
        }
        return getCurrentSession().createQuery(
                "  from NetworkGroup netgrp " +
                " where netgrp.networkGroupCategory is not null" +
                "   and netgrp.id not in (" +
                "       select rules.networkGroup " +
                "         from FirewallRule rules" +
                "        where rules.firewall=:firewall)")
                .setEntity("firewall", excludeFrom)
                .list();
    }
}
