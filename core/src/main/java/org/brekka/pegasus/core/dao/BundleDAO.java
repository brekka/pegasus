/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.Bundle;

/**
 * @author Andrew Taylor
 *
 */
public interface BundleDAO extends EntityDAO<UUID, Bundle> {

    /**
     * @param maxBundleCount
     * @return
     */
    List<Bundle> retrieveOldestExpired(int maxBundleCount);

    /**
     * @param bundle
     */
    void refresh(Bundle bundle);

}
