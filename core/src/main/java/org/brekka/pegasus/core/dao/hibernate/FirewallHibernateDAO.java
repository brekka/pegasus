/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.util.List;
import java.util.UUID;

import org.brekka.pegasus.core.dao.FirewallDAO;
import org.brekka.pegasus.core.model.Firewall;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Repository
public class FirewallHibernateDAO extends AbstractPegasusHibernateDAO<Firewall> implements FirewallDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Firewall> type() {
        return Firewall.class;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.FirewallDAO#retrieveByOwner(java.util.UUID)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Firewall> retrieveByOwningEntity(UUID owningEntityId) {
        return getCurrentSession().createCriteria(Firewall.class)
                .add(Restrictions.eq("owningEntityId", owningEntityId))
                .list();
    }
}
