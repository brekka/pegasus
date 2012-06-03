/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.Allocation;

/**
 * @author Andrew Taylor
 *
 */
public interface AllocationDAO extends EntityDAO<UUID, Allocation> {

    /**
     * @param bundle
     * @return 
     */
    void refresh(Allocation allocation);

}
