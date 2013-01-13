/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.util.List;

import org.brekka.pegasus.core.dao.InvitationDAO;
import org.brekka.pegasus.core.model.Invitation;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Token;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Repository
public class InvitationHibernateDAO extends AbstractPegasusHibernateDAO<Invitation> implements InvitationDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Invitation> type() {
        return Invitation.class;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<Invitation> retrieveForMember(Member member) {
        return getCurrentSession().createCriteria(Invitation.class)
                .add(Restrictions.eq("recipient", member))
                .list();
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.InvitationDAO#retrieveByToken(org.brekka.pegasus.core.model.Token)
     */
    @Override
    public Invitation retrieveByToken(Token token) {
        return (Invitation) getCurrentSession().createCriteria(Invitation.class)
                .add(Restrictions.eq("token", token))
                .uniqueResult();
    }
}
