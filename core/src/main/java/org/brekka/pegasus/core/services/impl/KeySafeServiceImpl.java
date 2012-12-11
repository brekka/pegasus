/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.UUID;

import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Partnership;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.KeySafeService;
import org.brekka.phalanx.api.beans.IdentityCryptedData;
import org.brekka.phalanx.api.beans.IdentityKeyPair;
import org.brekka.phalanx.api.beans.IdentityPrincipal;
import org.brekka.phalanx.api.model.CryptedData;
import org.brekka.phalanx.api.model.KeyPair;
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
        PrivateKeyToken privateKey = resolvePrivateKeyFor(keySafe, currentMember);
        data = phalanxService.asymDecrypt(new IdentityCryptedData(cryptedDataId), privateKey);
        return data;
    }
    
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.KeySafeService#createPartnership(org.brekka.pegasus.core.model.Actor, org.brekka.pegasus.core.model.Division, org.brekka.pegasus.core.model.Division)
     */
    @Override
    public <Owner extends Actor> Partnership<Owner> createPartnership(Owner owner, Division<Owner> source,
            Division<?> target) {
        IdentityKeyPair sourceKeyPair = new IdentityKeyPair(source.getKeyPairId());
        KeyPair connectionKeyPair = phalanxService.generateKeyPair(sourceKeyPair);
        
        Partnership<Owner> partnership = new Partnership<>();
        partnership.setOwner(owner);
        partnership.setSource(source);
        partnership.setTarget(target);
        partnership.setKeyPairId(connectionKeyPair.getId());
        connectionDAO.create(partnership);
        return partnership;
    }

}
