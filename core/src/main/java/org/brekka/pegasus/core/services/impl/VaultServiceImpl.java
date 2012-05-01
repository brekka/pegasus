/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.List;
import java.util.UUID;

import org.brekka.pegasus.core.dao.VaultDAO;
import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.OpenVault;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.VaultService;
import org.brekka.pegasus.core.utils.SlugUtils;
import org.brekka.phalanx.api.beans.IdentityCryptedData;
import org.brekka.phalanx.api.beans.IdentityPrincipal;
import org.brekka.phalanx.api.model.AuthenticatedPrincipal;
import org.brekka.phalanx.api.model.CryptedData;
import org.brekka.phalanx.api.model.Principal;
import org.brekka.phalanx.api.services.PhalanxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Andrew Taylor
 *
 */
@Service
@Transactional
public class VaultServiceImpl implements VaultService {

    @Autowired
    private VaultDAO vaultDAO;
    
    @Autowired
    private PhalanxService phalanxService;
    
    @Autowired
    private MemberService memberService;
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.VaultService#createVault(org.brekka.pegasus.core.model.Member, java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Vault createVault(String name, String vaultPassword, Member owner) {
        Vault vault = new Vault();
        vault.setOwner(owner);
        vault.setName(name);
        vault.setSlug(SlugUtils.sluggify(name));
        
        Principal principal = phalanxService.createPrincipal(vaultPassword);
        vault.setPrincipalId(principal.getId());
        
        vaultDAO.create(vault);
        return vault;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.VaultService#retrieveById(java.util.UUID)
     */
    @Override
    public Vault retrieveById(UUID vaultId) {
        return vaultDAO.retrieveById(vaultId);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.VaultService#retrieveForUser()
     */
    @Override
    public List<Vault> retrieveForUser() {
        AuthenticatedMember current = memberService.getCurrent();
        return vaultDAO.retrieveForMember(current.getMember());
    }
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public OpenVault openVault(Vault vault, String vaultPassword) {
        UUID principalId = vault.getPrincipalId();
        AuthenticatedPrincipal authenticatedPrincipal = phalanxService.authenticate(new IdentityPrincipal(principalId), vaultPassword);
        OpenVault openVault = new OpenVaultImpl(vault, authenticatedPrincipal);
        
        AuthenticatedMember current = memberService.getCurrent();
        AuthenticatedMemberImpl currentMemberImpl = (AuthenticatedMemberImpl) current;
        currentMemberImpl.retainVault(openVault);
        return openVault;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.VaultService#releaseKey(java.util.UUID, org.brekka.pegasus.core.model.OpenVault)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public byte[] releaseKey(UUID cryptedDataId, OpenVault openVault) {
        OpenVaultImpl ovi = (OpenVaultImpl) openVault;
        
        AuthenticatedPrincipal authenticatedPrincipal = ovi.getAuthenticatedPrincipal();
        CryptedData cryptedData = new IdentityCryptedData(cryptedDataId);
        
        byte[] secretKeyBytes = phalanxService.asymDecrypt(cryptedData, authenticatedPrincipal.getDefaultPrivateKey());
        return secretKeyBytes;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.VaultService#retrieveBySlug(java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Vault retrieveBySlug(String vaultSlug) {
        AuthenticatedMember current = memberService.getCurrent();
        Member member = current.getMember();
        Vault vault = vaultDAO.retrieveBySlug(vaultSlug, member);
        return vault;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.VaultService#isOpen(org.brekka.pegasus.core.model.Vault)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public boolean isOpen(Vault vault) {
        AuthenticatedMember current = memberService.getCurrent();
        AuthenticatedMemberImpl currentMemberImpl = (AuthenticatedMemberImpl) current;
        OpenVault openVault = currentMemberImpl.retrieveVault(vault.getId());
        return (openVault != null);
    }
}
