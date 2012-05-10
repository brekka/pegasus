/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.EMailAddressDAO;
import org.brekka.pegasus.core.model.EMailAddress;
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
}
