/**
 *
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.util.List;

import org.brekka.pegasus.core.dao.AssociateDAO;
import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Organization;
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
    public List<Associate> retrieveAssociates(final Member member) {
        return getCurrentSession().createCriteria(Associate.class)
                .add(Restrictions.eq("member", member))
                .list();
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.AssociateDAO#retrieveForOrganization(org.brekka.pegasus.core.model.Organization)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Associate> retrieveForOrganization(final Organization organization) {
        return getCurrentSession().createCriteria(Associate.class)
                .add(Restrictions.eq("organization", organization))
                .list();
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.AssociateDAO#retrieveByOrgAndMember(org.brekka.pegasus.core.model.Organization, org.brekka.pegasus.core.model.Member)
     */
    @Override
    public Associate retrieveByOrgAndMember(final Organization organization, final Member member) {
        return (Associate) getCurrentSession().createCriteria(Associate.class)
                .add(Restrictions.eq("member", member))
                .add(Restrictions.eq("organization", organization))
                .uniqueResult();
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.AssociateDAO#retrieveByMember(org.brekka.pegasus.core.model.Member)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Associate> retrieveByMember(final Member member) {
        return getCurrentSession().createCriteria(Associate.class)
                .add(Restrictions.eq("member", member))
                .list();
    }
}
