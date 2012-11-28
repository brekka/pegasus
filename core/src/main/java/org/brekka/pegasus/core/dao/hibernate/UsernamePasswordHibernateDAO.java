/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.UsernamePasswordDAO;
import org.brekka.pegasus.core.model.UsernamePassword;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Repository
public class UsernamePasswordHibernateDAO extends AbstractPegasusHibernateDAO<UsernamePassword> implements UsernamePasswordDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<UsernamePassword> type() {
        return UsernamePassword.class;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.MemberDAO#retrieveByOpenId(java.lang.String)
     */
    @Override
    public UsernamePassword retrieveByUsernameDigest(byte[] usernameDigest) {
        return (UsernamePassword) getCurrentSession().createCriteria(UsernamePassword.class)
                .add(Restrictions.eq("usernameDigest", usernameDigest))
                .uniqueResult();
    }
}
