/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import org.brekka.pegasus.core.model.OpenVault;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.phalanx.api.model.AuthenticatedPrincipal;

/**
 * @author Andrew Taylor
 *
 */
public class OpenVaultImpl implements OpenVault {
    
    private Vault vault;
    
    private AuthenticatedPrincipal authenticatedPrincipal;
  
    
    public OpenVaultImpl(Vault vault, AuthenticatedPrincipal authenticatedPrincipal) {
        this.vault = vault;
        this.authenticatedPrincipal = authenticatedPrincipal;
    }

    /**
     * @return the authenticatedPrincipal
     */
    AuthenticatedPrincipal getAuthenticatedPrincipal() {
        return authenticatedPrincipal;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.OpenVault#getVault()
     */
    @Override
    public Vault getVault() {
        return vault;
    }

}
