/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO;
import org.brekka.commons.persistence.model.IdentifiableEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public abstract class AbstractPegasusHibernateDAO<Entity extends IdentifiableEntity> extends AbstractIdentifiableEntityHibernateDAO<Entity>  {


    @Autowired
    private SessionFactory pegasusSessionFactory;
    
    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#getCurrentSession()
     */
    @Override
    protected Session getCurrentSession() {
        return pegasusSessionFactory.getCurrentSession();
    }
    
}
