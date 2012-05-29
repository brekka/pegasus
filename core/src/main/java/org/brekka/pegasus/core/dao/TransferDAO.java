/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.Bundle;
import org.brekka.pegasus.core.model.Transfer;

/**
 * @author Andrew Taylor
 *
 */
public interface TransferDAO extends EntityDAO<UUID, Transfer> {

    /**
     * @param bundle
     * @return
     */
    List<Transfer> retrieveByBundle(Bundle bundle);


}
