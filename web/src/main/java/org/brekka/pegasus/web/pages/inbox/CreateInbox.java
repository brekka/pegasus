/**
 * 
 */
package org.brekka.pegasus.web.pages.inbox;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.model.TokenType;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.InboxService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.VaultService;
import org.brekka.pegasus.web.pages.member.MemberIndex;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class CreateInbox {
    
    @Inject
    private InboxService inboxService;
    
    @Inject
    private VaultService vaultService;
    
    @Inject
    private MemberService memberService;
    
    @Inject
    private AlertManager alertManager;
    
    @Property
    private Vault selectedVault;

    @Property
    private String name;
    
    @Property
    private String inboxToken;
    
    @Property
    private String introduction;
    
    @SuppressWarnings("unused")
    @Property
    private final ValueEncoder<Vault> vaultEncoder = new ValueEncoder<Vault>() {
        @Override
        public String toClient(Vault vault) {
            return vault.getId().toString();
        }
        @Override
        public Vault toValue(String clientValue) {
            return vaultService.retrieveById(UUID.fromString(clientValue));
        }
    };
    
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
        List<Vault> vaultList = vaultService.retrieveForUser();
        List<OptionModel> options = new ArrayList<>(vaultList.size());
        for (Vault vault : vaultList) {
            options.add(new OptionModelImpl(vault.getName(), vault));
        }
        return new SelectModelImpl(null, options);
    }
}
