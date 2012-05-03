/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.UUID;

import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.KeySafeService;
import org.brekka.phalanx.api.beans.IdentityKeyPair;
import org.brekka.phalanx.api.beans.IdentityPrincipal;
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
        // TODO Auto-generated method stub
        return null;
    }

}
