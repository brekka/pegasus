/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.ProfileDAO;
import org.brekka.pegasus.core.model.Profile;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Repository
public class ProfileHibernateDAO  extends AbstractPegasusHibernateDAO<Profile> implements ProfileDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Profile> type() {
        return Profile.class;
    }
}
