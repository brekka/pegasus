/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.List;
import java.util.UUID;

import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.phalanx.api.model.KeyPair;
import org.brekka.phalanx.api.model.PrivateKeyToken;

/**
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface VaultService {

    Vault createVault(String name, String vaultPassword, Member owner);
    
    void openVault(Vault vault, String vaultPassword);

    /**
     * @param fromString
     * @return
     */
    Vault retrieveById(UUID fromString);

    /**
     * @return
     */
    List<Vault> retrieveForUser();

    /**
     * @param cryptedDataId
     * @param openVault
     * @return
     */
    byte[] releaseKey(UUID cryptedDataId, Vault vault);

    /**
     * @param vaultSlug
     * @return
     */
    Vault retrieveBySlug(String vaultSlug);

    /**
     * @param vault
     * @return
     */
    boolean isOpen(Vault vault);

    /**
     * @param toMemberVault
     * @return
     */
    KeyPair createKeyPair(Vault toMemberVault);

    /**
     * @param keyPair
     * @param toMemberVault
     * @return
     */
    PrivateKeyToken releaseKeyPair(KeyPair keyPair, Vault vault);

    /**
     * @param fromString
     */
    void closeVault(UUID fromString);

    /**
     * @param defaultVault
     * @param oldPassword
     * @param newPassword
     */
    void changePassword(Vault defaultVault, String oldPassword, String newPassword);
}
