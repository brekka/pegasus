/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.UUID;

import org.brekka.pegasus.core.dao.VaultDAO;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.OpenVault;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.VaultService;
import org.brekka.phalanx.api.beans.IdentityPrincipal;
import org.brekka.phalanx.api.model.AuthenticatedPrincipal;
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
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.VaultService#createVault(org.brekka.pegasus.core.model.Member, java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Vault createVault(String name, String vaultPassword, Member owner) {
        Vault vault = new Vault();
        vault.setOwner(owner);
        vault.setName(name);
        vault.setToken(sluggify(name));
        
        Principal principal = phalanxService.createPrincipal(vaultPassword);
        vault.setPrincipalId(principal.getId());
        
        
        
        vaultDAO.create(vault);
        return vault;
    }
    
    @Override
    public OpenVault openVault(Vault vault, String vaultPassword) {
        UUID principalId = vault.getPrincipalId();
        AuthenticatedPrincipal authenticatedPrincipal = phalanxService.authenticate(new IdentityPrincipal(principalId), vaultPassword);
        return new OpenVaultImpl(vault, authenticatedPrincipal);
    }

    /**
     * @param name
     * @return
     */
    private String sluggify(String name) {
        name = name.replaceAll("\\W+", "_");
        name = name.toLowerCase();
        return name;
    }

}
