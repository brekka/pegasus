/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.util.List;

import org.brekka.pegasus.core.dao.AssociateDAO;
import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Vault;
import org.hibernate.criterion.Restrictions;
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
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.AssociateDAO#retrieveAssociatesInKeySafe(org.brekka.pegasus.core.model.KeySafe)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Associate> retrieveAssociatesInVault(Vault vault) {
        return getCurrentSession().createCriteria(Associate.class)
                .add(Restrictions.eq("defaultVault", vault))
                .list();
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.AssociateDAO#retrieveByOrgAndMember(org.brekka.pegasus.core.model.Organization, org.brekka.pegasus.core.model.Member)
     */
    @Override
    public Associate retrieveByOrgAndMember(Organization organization, Member member) {
        return (Associate) getCurrentSession().createCriteria(Associate.class)
                .add(Restrictions.eq("member", member))
                .add(Restrictions.eq("organization", organization))
                .uniqueResult();
    }
}