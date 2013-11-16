/**
 *
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.sql.Timestamp;
import java.util.List;

import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.pegasus.core.dao.AllocationFileDAO;
import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.model.AllocationFile;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Repository
public class AllocationFileHibernateDAO extends AbstractPegasusHibernateDAO<AllocationFile> implements AllocationFileDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<AllocationFile> type() {
        return AllocationFile.class;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.AllocationFileDAO#refresh(org.brekka.pegasus.core.model.AllocationFile)
     */
    @Override
    public void refresh(final AllocationFile allocationFile) {
        getCurrentSession().refresh(allocationFile);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.AllocationFileDAO#retrieveByBundle(org.brekka.pegasus.core.model.Bundle)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<AllocationFile> retrieveByAllocation(final Allocation allocation) {
        return getCurrentSession().createCriteria(AllocationFile.class)
                .add(Restrictions.eq("allocation", allocation))
                .list();
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.AllocationFileDAO#retrieveOldestExpired(int)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<AllocationFile> retrieveOldestExpired(final int maxFileCount) {
        return getCurrentSession().createQuery(
                "select af from AllocationFile af " +
                " where af.deleted is null " +
                "   and af.expires < :now " +
                " order by expires asc "
            )
            .setParameter("now", new Timestamp(System.currentTimeMillis()))
            .setMaxResults(maxFileCount)
            .list();
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.AllocationFileDAO#retrieveActiveForCryptedFile(java.util.UUID)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<AllocationFile> retrieveActiveForCryptedFile(final CryptedFile cryptedFile) {
        return getCurrentSession().createCriteria(AllocationFile.class)
                .add(Restrictions.eq("cryptedFile", cryptedFile))
                .add(Restrictions.isNull("deleted"))
                .list();
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.AllocationFileDAO#retrieveActiveForAllocation(org.brekka.pegasus.core.model.Allocation)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<AllocationFile> retrieveActiveForAllocation(final Allocation allocation) {
        return getCurrentSession().createCriteria(AllocationFile.class)
                .add(Restrictions.eq("allocation", allocation))
                .add(Restrictions.isNull("deleted"))
                .list();
    }

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.hibernate.AbstractIdentifiableEntityHibernateDAO#update(org.brekka.commons.persistence.model.IdentifiableEntity)
     */
    @Override
    public void update(final AllocationFile entity) {
        super.update(entity);
        getCurrentSession().flush();
    }
}
