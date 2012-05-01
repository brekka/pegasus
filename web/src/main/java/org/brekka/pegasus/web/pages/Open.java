/**
 * 
 */
package org.brekka.pegasus.web.pages;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.VaultService;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class Open {
    @Inject
    private VaultService vaultService;
    
    @Property
    private String vaultPassword;
    
    @Property
    private Vault vault;

    Object onActivate(String vaultSlug) {
        Object retVal;
        vault = vaultService.retrieveBySlug(vaultSlug);
        if (vaultService.isOpen(vault)) {
            retVal = Member.class;
        } else {
            retVal = Boolean.TRUE;
        }
        return retVal;
    }
    
    String onPassivate() {
        return vault.getSlug();
    }
    
    Object onSuccess() {
        vaultService.openVault(vault, vaultPassword);
        return Member.class;
    }
}
