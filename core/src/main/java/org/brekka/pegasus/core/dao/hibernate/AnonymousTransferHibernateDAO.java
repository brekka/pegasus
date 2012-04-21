/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.AnonymousTransferDAO;
import org.brekka.pegasus.core.model.AnonymousTransfer;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor
 *
 */
@Repository
public class AnonymousTransferHibernateDAO extends AbstractPegasusHibernateDAO<AnonymousTransfer> implements AnonymousTransferDAO {
    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<AnonymousTransfer> type() {
        return AnonymousTransfer.class;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.AnonymousTransferDAO#retrieveBySlug(java.lang.String)
     */
    @Override
    public AnonymousTransfer retrieveBySlug(String slug) {
        return (AnonymousTransfer) getCurrentSession().createQuery(
                "select anon " +
                "  from AnonymousTransfer anon " +
                " inner join anon.slug as slug " +
                "  with slug.path=:slug")
                .setString("slug", slug)
                .uniqueResult();
    }
}
