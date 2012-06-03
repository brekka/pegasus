/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.model.AllocationFile;

/**
 * @author Andrew Taylor
 *
 */
public interface AllocationFileDAO extends EntityDAO<UUID, AllocationFile> {

    /**
     * @param bundle
     * @return 
     */
    List<AllocationFile> retrieveByAllocation(Allocation allocation);

    /**
     * @param maxFileCount
     * @return
     */
    List<AllocationFile> retrieveOldestExpired(int maxFileCount);

}
