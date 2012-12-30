/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.ActorDAO;
import org.brekka.pegasus.core.model.Actor;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Repository
public class ActorHibernateDAO extends AbstractPegasusHibernateDAO<Actor> implements ActorDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Actor> type() {
        return Actor.class;
    }
}
