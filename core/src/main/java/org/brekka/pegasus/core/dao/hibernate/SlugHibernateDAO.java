/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.SlugDAO;
import org.brekka.pegasus.core.model.Slug;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor
 *
 */
@Repository
public class SlugHibernateDAO extends AbstractPegasusHibernateDAO<Slug> implements SlugDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Slug> type() {
        return Slug.class;
    }

}
