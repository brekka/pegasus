/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.TransferUnlockEvent;

/**
 * @author Andrew Taylor
 *
 */
public interface BundleUnlockEventDAO extends EntityDAO<UUID, TransferUnlockEvent> {

}
