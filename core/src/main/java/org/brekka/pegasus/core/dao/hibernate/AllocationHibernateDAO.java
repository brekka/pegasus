/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.AllocationDAO;
import org.brekka.pegasus.core.model.Allocation;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Repository
public class AllocationHibernateDAO extends AbstractPegasusHibernateDAO<Allocation> implements AllocationDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Allocation> type() {
        return Allocation.class;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.AllocationDAO#refresh(org.brekka.pegasus.core.model.Allocation)
     */
    @Override
    public void refresh(Allocation allocation) {
        getCurrentSession().refresh(allocation);
    }
}
