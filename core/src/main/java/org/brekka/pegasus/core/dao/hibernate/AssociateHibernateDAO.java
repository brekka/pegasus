/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.AssociateDAO;
import org.brekka.pegasus.core.model.Associate;
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

}
