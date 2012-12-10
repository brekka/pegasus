/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.Dispatch;
import org.brekka.pegasus.core.model.KeySafe;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface DispatchDAO extends EntityDAO<UUID, Dispatch> {

    /**
     * @param activeActor
     * @param date
     * @param date2
     * @return
     */
    List<Dispatch> retrieveForInterval(KeySafe<?> keySafe, Actor activeActor, Date date, Date date2);

}
