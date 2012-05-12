/**
 * 
 */
package org.brekka.pegasus.web.pages.inbox;

import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.model.TokenType;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.InboxService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.web.pages.member.MemberIndex;
import org.brekka.pegasus.web.support.VaultEncoder;
import org.brekka.pegasus.web.support.VaultSelectModelBuilder;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class CreateInbox {
    
    @Inject
    private InboxService inboxService;
    
    @Inject
    private MemberService memberService;
    
    @Inject
    private AlertManager alertManager;
    
    @Inject
    private VaultEncoder vaultEncoder;
    
    @Inject
    private VaultSelectModelBuilder vaultSelectModelBuilder;
    
    @Property
    private Vault selectedVault;

    @Property
    private String name;
    
    @Property
    private String inboxToken;
    
    @Property
    private String introduction;
    
    Object onActivate() {
        Object retVal = Boolean.TRUE;
        selectedVault = memberService.getCurrent().getMember().getDefaultVault();
        inboxToken = TokenType.INBOX.generateRandom().getPath();
        return retVal;
    }
    
    Object onSuccess() {
        inboxService.createInbox(name, introduction, inboxToken, selectedVault);
        alertManager.alert(Duration.SINGLE, Severity.INFO, 
                String.format("Your new inbox '%s' has been created", name));
        return MemberIndex.class;
    }
    
    public SelectModel getVaultSelectModel() {
        return vaultSelectModelBuilder.getCurrent();
    }
    
    public VaultEncoder getVaultEncoder() {
        return vaultEncoder;
    }
}
