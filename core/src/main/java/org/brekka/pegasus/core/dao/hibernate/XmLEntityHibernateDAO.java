/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.io.InputStream;
import java.sql.Blob;

import org.apache.xmlbeans.XmlObject;
import org.brekka.pegasus.core.dao.XmlEntityDAO;
import org.brekka.pegasus.core.model.XmlEntity;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.engine.jdbc.LobCreator;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Repository
public class XmLEntityHibernateDAO<T extends XmlObject> extends AbstractPegasusHibernateDAO<XmlEntity<T>> implements XmlEntityDAO<T> {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected Class<XmlEntity<T>> type() {
        return (Class) XmlEntity.class;
    }
    
    @Override
    public void update(XmlEntity<T> profile, InputStream inputStream) {
        Session session = getCurrentSession();
        LobCreator lobCreator = Hibernate.getLobCreator(session);
        Blob blob = lobCreator.createBlob(inputStream, 0); // Doesn't even use the length
        profile.setData(blob);
        update(profile);
    }
}
