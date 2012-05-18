/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.UUID;

import org.brekka.pegasus.core.dao.DivisionAssociateDAO;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.DivisionAssociate;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.KeySafeService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.phalanx.api.beans.IdentityCryptedData;
import org.brekka.phalanx.api.beans.IdentityKeyPair;
import org.brekka.phalanx.api.beans.IdentityPrincipal;
import org.brekka.phalanx.api.model.AuthenticatedPrincipal;
import org.brekka.phalanx.api.model.CryptedData;
import org.brekka.phalanx.api.model.PrivateKeyToken;
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
    
    @Autowired
    private DivisionAssociateDAO  divisionAssociateDAO;
    
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
        AuthenticatedMemberBase currentMember = AuthenticatedMemberBase.getCurrent(memberService);
        PrivateKeyToken privateKey;
        if (keySafe instanceof Vault) {
            AuthenticatedPrincipal authenticatedPrincipal = currentMember.getVaultKey(keySafe.getId());
            privateKey = authenticatedPrincipal.getDefaultPrivateKey();
        } else if (keySafe instanceof Division) {
            Division division = (Division) keySafe;
            privateKey = identifyPrivateKey(division, currentMember);
        } else {
            throw new IllegalStateException();
        }
        data = phalanxService.asymDecrypt(new IdentityCryptedData(cryptedDataId), privateKey);
        return data;
    }

    /**
     * @param division
     * @param currentMember
     * @return
     */
    private PrivateKeyToken identifyPrivateKey(Division division, AuthenticatedMemberBase currentMember) {
        Actor activeActor = currentMember.getActiveActor();
        if (activeActor instanceof Associate == false) {
            // TODO
            throw new IllegalStateException();
        }
        Associate associate = (Associate) activeActor;
        return resolvePrivateKeyFor(division, associate, currentMember);
    }
    
    private PrivateKeyToken resolvePrivateKeyFor(Division division, Associate associate, AuthenticatedMemberBase currentMember) {
        PrivateKeyToken privateKeyToken;
        DivisionAssociate divisionAssociate = divisionAssociateDAO.retrieveBySurrogateKey(division, associate);
        if (divisionAssociate != null) {
            privateKeyToken = currentMember.getPrivateKey(divisionAssociate.getId());
            if (privateKeyToken == null) {
                UUID keyPairId = divisionAssociate.getKeyPairId();
                Vault vault = associate.getDefaultVault();
                AuthenticatedPrincipal vaultKey = currentMember.getVaultKey(vault.getId());
                PrivateKeyToken userPrivateKey = vaultKey.getDefaultPrivateKey();
                privateKeyToken = phalanxService.decryptKeyPair(new IdentityKeyPair(keyPairId), userPrivateKey);
                currentMember.retainPrivateKey(divisionAssociate.getId(), privateKeyToken);
            }
        } else {
            if (division.getParent() != null) {
                privateKeyToken = currentMember.getPrivateKey(division.getId());
                if (privateKeyToken == null) {
                    PrivateKeyToken parentPrivateKeyToken = resolvePrivateKeyFor(division.getParent(), associate, currentMember);
                    UUID keyPairId = division.getKeyPairId();
                    privateKeyToken = phalanxService.decryptKeyPair(new IdentityKeyPair(keyPairId), parentPrivateKeyToken);
                    currentMember.retainPrivateKey(division.getId(), privateKeyToken);
                }
            } else {
                throw new IllegalStateException("The user does not have access to this container");
            }
        }
        return privateKeyToken;
    }

}
