/**
 *
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.util.List;
import java.util.UUID;

import org.apache.xmlbeans.XmlObject;
import org.brekka.pegasus.core.dao.XmlEntityDAO;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.XmlEntity;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
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
    public <T extends XmlObject> XmlEntity<T> retrieveBySerialVersion(final UUID serial, final int version, final Class<T> xmlType) {
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
    public List<XmlEntity<?>> retrieveByKeySafe(final KeySafe<?> keySafe) {
        return getCurrentSession().createCriteria(XmlEntity.class)
                .add(Restrictions.eq("keySafe", keySafe))
                .list();
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.dao.XmlEntityDAO#retrieveBySeries(java.util.UUID)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<XmlEntity<?>> retrieveBySerial(final UUID serial) {
        return getCurrentSession().createCriteria(XmlEntity.class)
                .add(Restrictions.eq("serial", serial))
                .list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<XmlEntity<?>> findExternalResourceBased(final UUID afterId, final int limit) {
        return getCurrentSession().createCriteria(XmlEntity.class)
                .add(Restrictions.gt("id", afterId))
                .add(Restrictions.eq("externalData", true))
                .setMaxResults(limit)
                .addOrder(Order.asc("id"))
                .list();
    }
}
