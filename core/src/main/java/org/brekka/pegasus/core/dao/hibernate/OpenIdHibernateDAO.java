/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.OpenIdDAO;
import org.brekka.pegasus.core.model.OpenID;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Repository
public class OpenIdHibernateDAO extends AbstractPegasusHibernateDAO<OpenID> implements OpenIdDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<OpenID> type() {
        return OpenID.class;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.MemberDAO#retrieveByOpenId(java.lang.String)
     */
    @Override
    public OpenID retrieveByURI(String openIdUri) {
        return (OpenID) getCurrentSession().createCriteria(OpenID.class)
                .add(Restrictions.eq("uri", openIdUri))
                .uniqueResult();
    }
}
