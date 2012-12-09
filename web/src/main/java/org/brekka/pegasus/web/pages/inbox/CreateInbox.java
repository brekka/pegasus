/**
 * 
 */
package org.brekka.pegasus.web.pages.inbox;

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.TokenType;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.DivisionService;
import org.brekka.pegasus.core.services.InboxService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.OrganizationService;
import org.brekka.pegasus.core.services.VaultService;
import org.brekka.pegasus.web.pages.member.MemberIndex;
import org.brekka.pegasus.web.pages.org.OrgIndex;
import org.brekka.pegasus.web.support.VaultEncoder;
import org.brekka.pegasus.web.support.VaultSelectModelBuilder;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class CreateInbox {
    @InjectPage
    private OrgIndex orgIndexPage;
    
    @Inject
    private OrganizationService organizationService;
    
    @Inject
    private DivisionService divisionService;
    
    @Inject
    private InboxService inboxService;
    
    @Inject
    private MemberService memberService;
    
    @Inject
    private VaultService vaultService;
    
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
    
    @Property
    private Division division;
    
    private Object[] context;
    
    Object onActivate() {
        return onActivate(null);
    }
    
    Object onActivate(String vaultSlug) {
        if (StringUtils.isBlank(vaultSlug)) {
            selectedVault = memberService.getCurrent().getMember().getDefaultVault();
        } else {
            selectedVault = vaultService.retrieveBySlug(vaultSlug);
        }
        return activate(selectedVault, vaultSlug);
    }
    
    Object onActivate(String orgToken, String divisionSlug) {
        Organization organization = organizationService.retrieveByToken(orgToken);
        division = divisionService.retrieveDivision(organization, divisionSlug);
        return activate(orgToken, divisionSlug);
    }
    
    Object activate(Object... context) {
        this.context = context;
        this.inboxToken = TokenType.INBOX.generateRandom().getPath();
        return Boolean.TRUE;
    }
    
    Object[] onPassivate() {
        return context;
    }
    
    Object onSuccess() {
        Object retVal;
        if (division == null) {
            inboxService.createInbox(name, introduction, inboxToken, selectedVault);
            retVal = MemberIndex.class;
        } else {
            inboxService.createInbox(name, introduction, inboxToken, division);
            orgIndexPage.init(division.getOrganization());
            retVal = orgIndexPage;
        }
        alertManager.alert(Duration.SINGLE, Severity.INFO, 
                String.format("The new inbox '%s' has been created", name));
        return retVal;
    }
    
    public SelectModel getVaultSelectModel() {
        return vaultSelectModelBuilder.getCurrent();
    }
    
    public VaultEncoder getVaultEncoder() {
        return vaultEncoder;
    }
}
