/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.DispatchDAO;
import org.brekka.pegasus.core.model.Dispatch;
import org.springframework.stereotype.Repository;

/**
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Repository
public class DispatchHibernateDAO extends AbstractPegasusHibernateDAO<Dispatch> implements DispatchDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Dispatch> type() {
        return Dispatch.class;
    }

}
