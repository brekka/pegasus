/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.util.List;

import org.brekka.pegasus.core.dao.AssociateDAO;
import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.KeySafe;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Repository
public class AssociateHibernateDAO extends AbstractPegasusHibernateDAO<Associate> implements AssociateDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Associate> type() {
        return Associate.class;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.AssociateDAO#retrieveAssociatesInKeySafe(org.brekka.pegasus.core.model.KeySafe)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Associate> retrieveAssociatesInKeySafe(KeySafe keySafe) {
        return getCurrentSession().createCriteria(Associate.class)
                .add(Restrictions.eq("keySafe", keySafe))
                .list();
    }

}
