/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.FileDownloadEvent;

/**
 * @author Andrew Taylor
 *
 */
public interface FileDownloadEventDAO extends EntityDAO<UUID, FileDownloadEvent> {

}
