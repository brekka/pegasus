/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.io.InputStream;
import java.sql.Blob;

import org.brekka.pegasus.core.dao.ProfileDAO;
import org.brekka.pegasus.core.model.Profile;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.engine.jdbc.LobCreator;
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
    
    @Override
    public void update(Profile profile, InputStream inputStream) {
        Session session = getCurrentSession();
        LobCreator lobCreator = Hibernate.getLobCreator(session);
        Blob blob = lobCreator.createBlob(inputStream, 0); // Doesn't even use the length
        profile.setData(blob);
        update(profile);
    }
}
