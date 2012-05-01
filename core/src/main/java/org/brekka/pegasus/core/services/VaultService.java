/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.List;
import java.util.UUID;

import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.OpenVault;
import org.brekka.pegasus.core.model.Vault;

/**
 * @author Andrew Taylor
 *
 */
public interface VaultService {

    Vault createVault(String name, String vaultPassword, Member owner);
    
    OpenVault openVault(Vault vault, String vaultPassword);

    /**
     * @param fromString
     * @return
     */
    Vault retrieveById(UUID fromString);

    /**
     * @return
     */
    List<Vault> retrieveForUser();
}
