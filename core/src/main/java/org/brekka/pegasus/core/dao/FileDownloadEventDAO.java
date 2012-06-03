/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.AllocationFile;
import org.brekka.pegasus.core.model.FileDownloadEvent;
import org.brekka.pegasus.core.model.Transfer;

/**
 * @author Andrew Taylor
 *
 */
public interface FileDownloadEventDAO extends EntityDAO<UUID, FileDownloadEvent> {

    /**
     * @param bundleFile
     * @param transfer
     * @return
     */
    int fileDownloadCount(AllocationFile bundleFile, Transfer transfer);

}
