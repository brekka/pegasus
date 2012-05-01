/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.util.List;

import org.brekka.pegasus.core.dao.InboxDAO;
import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Token;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Repository
public class InboxHibernateDAO extends AbstractPegasusHibernateDAO<Inbox> implements InboxDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Inbox> type() {
        return Inbox.class;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.InboxDAO#retrieveByToken(java.lang.String)
     */
    @Override
    public Inbox retrieveByToken(Token token) {
        return (Inbox) getCurrentSession().createCriteria(Inbox.class)
                .add(Restrictions.eq("token", token))
                .uniqueResult();
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.InboxDAO#retrieveForMember(org.brekka.pegasus.core.model.Member)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Inbox> retrieveForMember(Member member) {
        return getCurrentSession().createCriteria(Inbox.class)
                .add(Restrictions.eq("owner", member))
                .list();
    }
}
