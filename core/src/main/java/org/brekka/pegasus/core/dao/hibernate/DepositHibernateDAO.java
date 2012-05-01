/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.DepositDAO;
import org.brekka.pegasus.core.model.Deposit;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 * 
 */
@Repository
public class DepositHibernateDAO extends AbstractPegasusHibernateDAO<Deposit> implements DepositDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Deposit> type() {
        return Deposit.class;
    }
}
