/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.MemberDAO;
import org.brekka.pegasus.core.model.Member;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor
 *
 */
@Repository
public class MemberHibernateDAO extends AbstractPegasusHibernateDAO<Member> implements MemberDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Member> type() {
        return Member.class;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.MemberDAO#retrieveByOpenId(java.lang.String)
     */
    @Override
    public Member retrieveByOpenId(String openId) {
        return (Member) getCurrentSession().createCriteria(Member.class)
                .add(Restrictions.eq("openId", openId))
                .uniqueResult();
    }

}
