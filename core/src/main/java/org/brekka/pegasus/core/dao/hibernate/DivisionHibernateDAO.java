/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.DivisionDAO;
import org.brekka.pegasus.core.model.Division;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Repository
public class DivisionHibernateDAO extends AbstractPegasusHibernateDAO<Division> implements DivisionDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Division> type() {
        return Division.class;
    }

}
