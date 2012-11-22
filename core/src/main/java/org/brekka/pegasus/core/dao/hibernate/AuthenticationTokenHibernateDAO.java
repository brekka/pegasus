/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.AuthenticationTokenDAO;
import org.brekka.pegasus.core.model.AuthenticationToken;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Repository
public class AuthenticationTokenHibernateDAO extends AbstractPegasusHibernateDAO<AuthenticationToken> implements AuthenticationTokenDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<AuthenticationToken> type() {
        return AuthenticationToken.class;
    }

}
