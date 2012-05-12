/**
 * 
 */
package org.brekka.pegasus.core.event;

import org.brekka.pegasus.core.model.Vault;
import org.springframework.context.ApplicationEvent;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class VaultOpenEvent extends ApplicationEvent {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -2208174907955584037L;

    public VaultOpenEvent(Vault vault) {
        super(vault);
    }
    
    public Vault getVault() {
        return (Vault) getSource();
    }
}
