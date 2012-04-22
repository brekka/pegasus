/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.BundleCreatedEvent;

/**
 * @author Andrew Taylor
 *
 */
public interface BundleCreatedEventDAO extends EntityDAO<UUID, BundleCreatedEvent> {

}
