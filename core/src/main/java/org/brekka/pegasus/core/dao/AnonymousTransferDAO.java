/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.AnonymousTransfer;

/**
 * @author Andrew Taylor
 *
 */
public interface AnonymousTransferDAO extends EntityDAO<UUID, AnonymousTransfer> {

    /**
     * @param slug
     * @return
     */
    AnonymousTransfer retrieveBySlug(String slug);

}
