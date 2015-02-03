/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.io.InputStream;
import java.sql.Blob;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlObject;
import org.brekka.pegasus.core.dao.XmlEntityDAO;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.XmlEntity;
import org.hibernate.Hibernate;
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
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.XmlEntityDAO#retrieveByVault(org.brekka.pegasus.core.model.Vault)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<XmlEntity<?>> retrieveByKeySafe(KeySafe<?> keySafe) {
        return getCurrentSession().createCriteria(XmlEntity.class)
                .add(Restrictions.eq("keySafe", keySafe))
                .list();
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.XmlEntityDAO#retrieveBySeries(java.util.UUID)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<XmlEntity<?>> retrieveBySerial(UUID serial) {
        return getCurrentSession().createCriteria(XmlEntity.class)
                .add(Restrictions.eq("serial", serial))
                .list();
    }
}
