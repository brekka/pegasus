/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.Bundle;
import org.brekka.pegasus.core.model.BundleFile;

/**
 * @author Andrew Taylor
 *
 */
public interface BundleFileDAO extends EntityDAO<UUID, BundleFile> {

    /**
     * @param bundle
     * @return 
     */
    List<BundleFile> retrieveByBundle(Bundle bundle);

}
