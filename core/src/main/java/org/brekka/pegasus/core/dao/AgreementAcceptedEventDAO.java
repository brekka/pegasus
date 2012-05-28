/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.AgreementAcceptedEvent;
import org.brekka.pegasus.core.model.Transfer;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface AgreementAcceptedEventDAO extends EntityDAO<UUID, AgreementAcceptedEvent> {

    /**
     * @param bundle
     * @return
     */
    AgreementAcceptedEvent retrieveByTransfer(Transfer transfer);
}
