/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.DivisionAssociate;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface DivisionAssociateDAO extends EntityDAO<UUID, DivisionAssociate> {

    /**
     * @param division
     * @param associate
     * @return
     */
    DivisionAssociate retrieveBySurrogateKey(Division division, Associate associate);

    /**
     * @param organization
     * @param associate
     * @return
     */
    List<DivisionAssociate> retrieveForOrg(Associate associate);

}
