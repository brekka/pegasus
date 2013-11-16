/**
 * Copyright (c) 2013 Digital Shadows Ltd.
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.KeySafeDAO;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.KeySafe;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor (andy@digitalshadows.com)
 *
 */
@Repository
public class KeySafeHibernateDAO extends AbstractPegasusHibernateDAO<KeySafe<? extends Actor>> implements KeySafeDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.hibernate.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected Class<KeySafe<? extends Actor>> type() {
        return (Class) KeySafe.class;
    }

}
