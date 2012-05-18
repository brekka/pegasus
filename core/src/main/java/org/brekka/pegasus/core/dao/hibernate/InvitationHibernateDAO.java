/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.util.List;

import org.brekka.pegasus.core.dao.InvitationDAO;
import org.brekka.pegasus.core.model.Invitation;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Vault;
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
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.InvitationDAO#retrieveForVault(org.brekka.pegasus.core.model.Member, org.brekka.pegasus.core.model.Vault)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Invitation> retrieveForVault(Member member, Vault vault) {
        return getCurrentSession().createQuery(
                "select inv " +
                "  from Invitation inv " +
                "  join inv.xml as xml" +
                " where inv.recipient=:recipient" +
                "   and xml.keySafe=:vault")
                .setEntity("recipient", member)
                .setEntity("vault", vault)
                .list();
    }
}
