/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.io.InputStream;
import java.sql.Blob;
import java.util.UUID;

import org.apache.xmlbeans.XmlObject;
import org.brekka.pegasus.core.dao.XmlEntityDAO;
import org.brekka.pegasus.core.model.XmlEntity;
import org.hibernate.Hibernate;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.jdbc.LobCreator;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Repository
public class XmlEntityHibernateDAO extends AbstractPegasusHibernateDAO<XmlEntity<?>> implements XmlEntityDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected Class<XmlEntity<?>> type() {
        return (Class) XmlEntity.class;
    }
    
    @Override
    public void create(XmlEntity<?> xmlEntity, InputStream inputStream, long length) {
        Session session = getCurrentSession();
        LobCreator lobCreator = Hibernate.getLobCreator(session);
        Blob blob = lobCreator.createBlob(inputStream, length);
        xmlEntity.setData(blob);
        create(xmlEntity);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.XmlEntityDAO#retrieveBySerialVersion(java.util.UUID, int, java.lang.Class, boolean)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends XmlObject> XmlEntity<T> retrieveBySerialVersion(UUID serial, int version, Class<T> xmlType) {
        return (XmlEntity<T>) getCurrentSession().createCriteria(XmlEntity.class)
                .add(Restrictions.eq("serial", serial))
                .add(Restrictions.eq("version", version))
                .uniqueResult();
    }
}
