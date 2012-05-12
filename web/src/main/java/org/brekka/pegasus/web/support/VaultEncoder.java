/**
 * 
 */
package org.brekka.pegasus.web.support;

import java.util.UUID;

import org.apache.tapestry5.ValueEncoder;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.VaultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Component
public class VaultEncoder implements ValueEncoder<Vault> {
    
    @Autowired
    private VaultService vaultService;
    
    @Override
    public String toClient(Vault vault) {
        return vault.getId().toString();
    }

    @Override
    public Vault toValue(String clientValue) {
        return vaultService.retrieveById(UUID.fromString(clientValue));
    }
}