/**
 * 
 */
package org.brekka.pegasus.web.pages.vault;

import org.apache.commons.lang.StringUtils;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.VaultService;
import org.brekka.pegasus.web.pages.member.MemberIndex;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class CreateVault {
    
    @InjectComponent
    private Form vault;
    
    @Inject
    private VaultService vaultService;
    
    @Inject
    private MemberService memberService;
    
    @Inject
    private AlertManager alertManager;

    @Property
    private String name;
    
    @Property
    private String vaultPassword;
    
    @Property
    private String vaultPasswordRepeat;
    
    
    Object onActivate() {
        Object retVal = Boolean.TRUE;
        return retVal;
    }
    
    void onValidateForm() {
        if (!StringUtils.equals(vaultPassword, vaultPasswordRepeat)) {
            vault.recordError("The passwords do not match");
        }
    }
    
    Object onSuccess() {
        Member member = memberService.getCurrent().getMember();
        Vault vault = vaultService.createVault(name, vaultPassword, member);
        vaultService.openVault(vault, vaultPassword);
        alertManager.alert(Duration.SINGLE, Severity.INFO, 
                String.format("Your new vault '%s' has been created", name));
        return MemberIndex.class;
    }
}
