/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.util.List;

import org.brekka.paveway.core.model.Bundle;
import org.brekka.pegasus.core.dao.TransferDAO;
import org.brekka.pegasus.core.model.Transfer;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Repository
public class TransferHibernateDAO extends AbstractPegasusHibernateDAO<Transfer> implements TransferDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Transfer> type() {
        return Transfer.class;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.TransferDAO#retrieveByBundle(org.brekka.pegasus.core.model.Bundle)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Transfer> retrieveByBundle(Bundle bundle) {
        return getCurrentSession().createCriteria(Transfer.class)
                .add(Restrictions.eq("bundle", bundle))
                .list();
    }
}
