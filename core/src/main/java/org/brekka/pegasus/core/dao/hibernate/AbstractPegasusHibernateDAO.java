/**
 *
 */
package org.brekka.pegasus.core.dao.hibernate;

import java.util.UUID;

import org.brekka.commons.persistence.dao.hibernate.AbstractUniversallyIdentifiableEntityHibernateDAO;
import org.brekka.commons.persistence.model.IdentifiableEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractPegasusHibernateDAO<Entity extends IdentifiableEntity<UUID>> extends AbstractUniversallyIdentifiableEntityHibernateDAO<Entity>  {


    @Autowired
    private SessionFactory pegasusSessionFactory;

    @Override
    protected Session getCurrentSession() {
        return pegasusSessionFactory.getCurrentSession();
    }

}
