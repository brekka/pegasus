/**
 * 
 */
package org.brekka.pegasus.web.support;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.VaultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Component
public class VaultSelectModelBuilder {
    
    @Autowired
    private VaultService vaultService;
    
    public SelectModel getCurrent() {
        List<Vault> vaultList = vaultService.retrieveForUser();
        List<OptionModel> options = new ArrayList<>(vaultList.size());
        for (Vault vault : vaultList) {
            options.add(new OptionModelImpl(vault.getName(), vault));
        }
        return new SelectModelImpl(null, options);
    }
}