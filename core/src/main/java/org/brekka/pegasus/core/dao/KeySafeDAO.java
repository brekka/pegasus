/**
 * Copyright (c) 2013 Digital Shadows Ltd.
 */
package org.brekka.pegasus.core.dao;

import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.KeySafe;

/**
 * @author Andrew Taylor (andy@digitalshadows.com)
 *
 */
public interface KeySafeDAO extends EntityDAO<UUID, KeySafe<? extends Actor>> {

}
