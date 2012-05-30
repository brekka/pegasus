/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.sql.Date;
import java.util.List;

import org.brekka.pegasus.core.dao.BundleFileDAO;
import org.brekka.pegasus.core.model.Bundle;
import org.brekka.pegasus.core.model.BundleFile;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Repository
public class BundleFileHibernateDAO extends AbstractPegasusHibernateDAO<BundleFile> implements BundleFileDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<BundleFile> type() {
        return BundleFile.class;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.BundleFileDAO#retrieveByBundle(org.brekka.pegasus.core.model.Bundle)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<BundleFile> retrieveByBundle(Bundle bundle) {
        return getCurrentSession().createCriteria(BundleFile.class)
                .add(Restrictions.eq("bundle", bundle))
                .list();
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.BundleFileDAO#retrieveOldestExpired(int)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<BundleFile> retrieveOldestExpired(int maxFileCount) {
        return getCurrentSession().createCriteria(BundleFile.class)
                .add(Restrictions.isNull("deleted"))
                .add(Restrictions.lt("expires", new Date(System.currentTimeMillis())))
                .setMaxResults(maxFileCount)
                .addOrder(Order.asc("expires"))
                .list();
    }
}
