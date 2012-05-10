/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.DomainNameDAO;
import org.brekka.pegasus.core.model.DomainName;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Repository
public class DomainNameHibernateDAO extends AbstractPegasusHibernateDAO<DomainName> implements DomainNameDAO {
    
    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<DomainName> type() {
        return DomainName.class;
    }
}
