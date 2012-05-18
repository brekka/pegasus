/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.EMailAddressDAO;
import org.brekka.pegasus.core.model.EMailAddress;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Repository
public class EMailAddressHibernateDAO extends AbstractPegasusHibernateDAO<EMailAddress> implements EMailAddressDAO {
    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<EMailAddress> type() {
        return EMailAddress.class;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.EMailAddressDAO#retrieveByHash(byte[])
     */
    @Override
    public EMailAddress retrieveByHash(byte[] hash) {
        return (EMailAddress) getCurrentSession().createCriteria(EMailAddress.class)
                .add(Restrictions.eq("hash", hash))
                .add(Restrictions.eq("active", Boolean.TRUE))
                .uniqueResult();
    }
}
