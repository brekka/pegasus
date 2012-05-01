/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Vault;

/**
 * @author Andrew Taylor
 *
 */
public interface VaultDAO extends EntityDAO<UUID, Vault> {

    
    /**
     * @param member
     * @return
     */
    List<Vault> retrieveForMember(Member member);

    /**
     * @param vaultSlug
     * @param member
     * @return
     */
    Vault retrieveBySlug(String vaultSlug, Member member);


}
