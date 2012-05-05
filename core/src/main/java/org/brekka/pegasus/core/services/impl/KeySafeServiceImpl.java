/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.UUID;

import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.OpenVault;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.KeySafeService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.phalanx.api.beans.IdentityCryptedData;
import org.brekka.phalanx.api.beans.IdentityKeyPair;
import org.brekka.phalanx.api.beans.IdentityPrincipal;
import org.brekka.phalanx.api.model.AuthenticatedPrincipal;
import org.brekka.phalanx.api.model.CryptedData;
import org.brekka.phalanx.api.services.PhalanxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Service
@Transactional
public class KeySafeServiceImpl implements KeySafeService {
    
    @Autowired
    protected PhalanxService phalanxService;
    
    @Autowired
    private MemberService memberService;
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.KeySafeService#protect(byte[], org.brekka.pegasus.core.model.KeySafe)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public CryptedData protect(byte[] keyData, KeySafe keySafe) {
        CryptedData cryptedData;
        if (keySafe instanceof Vault) {
            Vault vault = (Vault) keySafe;
            cryptedData = phalanxService.asymEncrypt(keyData, 
                    new IdentityPrincipal(vault.getPrincipalId()));
        } else if (keySafe instanceof Division) {
            Division division = (Division) keySafe;
            cryptedData = phalanxService.asymEncrypt(keyData, 
                    new IdentityKeyPair(division.getKeyPairId()));
        } else {
            throw new IllegalStateException();
        }
        return cryptedData;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.KeySafeService#release(java.util.UUID, org.brekka.pegasus.core.model.KeySafe)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public byte[] release(UUID cryptedDataId, KeySafe keySafe) {
        byte[] data;
        if (keySafe instanceof Vault) {
            AuthenticatedMember current = memberService.getCurrent();
            OpenVault openVault = current.getVault(keySafe.getId());
            OpenVaultImpl openVaultImpl = (OpenVaultImpl) openVault;
            AuthenticatedPrincipal authenticatedPrincipal = openVaultImpl.getAuthenticatedPrincipal();
            data = phalanxService.asymDecrypt(new IdentityCryptedData(cryptedDataId), 
                    authenticatedPrincipal.getDefaultPrivateKey());
        } else if (keySafe instanceof Division) {
            throw new UnsupportedOperationException("Not yet implemented");
        } else {
            throw new IllegalStateException();
        }
        return data;
    }

}
