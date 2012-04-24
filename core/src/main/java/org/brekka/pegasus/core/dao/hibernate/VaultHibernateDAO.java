/**
 * 
 */
package org.brekka.pegasus.core.dao.hibernate;

import org.brekka.pegasus.core.dao.VaultDAO;
import org.brekka.pegasus.core.model.Vault;
import org.springframework.stereotype.Repository;

/**
 * @author Andrew Taylor
 *
 */
@Repository
public class VaultHibernateDAO extends AbstractPegasusHibernateDAO<Vault> implements VaultDAO {

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.dao.impl.AbstractIdentifiableEntityHibernateDAO#type()
     */
    @Override
    protected Class<Vault> type() {
        return Vault.class;
    }
    

}
