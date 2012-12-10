/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.UUID;

import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.KeySafeService;
import org.brekka.phalanx.api.beans.IdentityCryptedData;
import org.brekka.phalanx.api.beans.IdentityKeyPair;
import org.brekka.phalanx.api.beans.IdentityPrincipal;
import org.brekka.phalanx.api.model.AuthenticatedPrincipal;
import org.brekka.phalanx.api.model.CryptedData;
import org.brekka.phalanx.api.model.PrivateKeyToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Service
@Transactional
public class KeySafeServiceImpl extends AbstractKeySafeServiceSupport implements KeySafeService {
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.KeySafeService#protect(byte[], org.brekka.pegasus.core.model.KeySafe)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public CryptedData protect(byte[] keyData, KeySafe<?> keySafe) {
        if (keySafe == null) {
            throw new IllegalArgumentException("A keySafe must be specified");
        }
        CryptedData cryptedData;
        if (keySafe instanceof Vault) {
            Vault vault = (Vault) keySafe;
            cryptedData = phalanxService.asymEncrypt(keyData, 
                    new IdentityPrincipal(vault.getPrincipalId()));
        } else if (keySafe instanceof Division) {
            Division<?> division = (Division<?>) keySafe;
            cryptedData = phalanxService.asymEncrypt(keyData, 
                    new IdentityKeyPair(division.getKeyPairId()));
        } else {
            throw new IllegalStateException("Unknown keySafe type: " + keySafe.getClass().getName());
        }
        return cryptedData;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.KeySafeService#release(java.util.UUID, org.brekka.pegasus.core.model.KeySafe)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public byte[] release(UUID cryptedDataId, KeySafe<?> keySafe) {
        if (keySafe == null) {
            throw new IllegalArgumentException("A keySafe must be specified");
        }
        byte[] data;
        AuthenticatedMemberBase<Member> currentMember = AuthenticatedMemberBase.getCurrent(memberService, Member.class);
        PrivateKeyToken privateKey;
        if (keySafe instanceof Vault) {
            AuthenticatedPrincipal authenticatedPrincipal = currentMember.getVaultKey(keySafe.getId());
            privateKey = authenticatedPrincipal.getDefaultPrivateKey();
        } else if (keySafe instanceof Division) {
            Division<?> division = (Division<?>) keySafe;
            privateKey = identifyPrivateKey(division, currentMember);
        } else {
            throw new IllegalStateException("Unknown keySafe type: " + keySafe.getClass().getName());
        }
        data = phalanxService.asymDecrypt(new IdentityCryptedData(cryptedDataId), privateKey);
        return data;
    }

}
