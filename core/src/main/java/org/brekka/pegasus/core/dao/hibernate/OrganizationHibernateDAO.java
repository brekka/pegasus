/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.OrganizationDAO;
import org.brekka.pegasus.core.model.Organization;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Repository
public class OrganizationHibernateDAO extends AbstractPegasusHibernateDAO<Organization> implements OrganizationDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Organization> type() {
        return Organization.class;
    }

}
