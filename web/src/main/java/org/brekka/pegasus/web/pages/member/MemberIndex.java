/**
 * 
 */
package org.brekka.pegasus.web.pages.member;

import java.util.List;
import java.util.UUID;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.Deposit;
import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.core.model.Invitation;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.InboxService;
import org.brekka.pegasus.core.services.InvitationService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.OrganizationService;
import org.brekka.pegasus.core.services.VaultService;
import org.brekka.pegasus.web.pages.org.OrgIndex;
import org.brekka.pegasus.web.support.MakeKeyUtils;

/**
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class MemberIndex {
    
    @InjectPage
    private OrgIndex orgIndexPage;
    
    @Inject
    private MemberService memberService;
    
    @Inject
    private InboxService inboxService;
    
    @Inject
    private VaultService vaultService;
    
    @Inject
    private OrganizationService organizationService;
    
    @Inject
    private InvitationService invitationService;
    
    @InjectComponent
    private Zone openVaultZone;
    
    @Component
    private Form openVault;
    
    @Property
    private Inbox loopInbox;
    
    @Property
    private Associate loopAssociate;
    
    @Property
    private Invitation loopInvitation;
    
    @Property
    private Vault loopVault;
    
    @Property
    private Vault selectedVault;
    
    @Property
    private String vaultPassword;
    
    Object onActivate() {
        Object retVal = Boolean.TRUE;
        if (memberService.isNewMember()) {
            // New member, go to the setup page
            retVal = SetupMember.class;
        } else {
            AuthenticatedMember current = memberService.getCurrent();
            if (current.getActiveActor() instanceof Associate) {
                // In org mode, redirect back to the corresponding organization home
                Associate associate = (Associate) current.getActiveActor();
                orgIndexPage.init(associate.getOrganization());
                retVal = orgIndexPage;
            } else {
                selectedVault = current.getMember().getDefaultVault();
            }
        }
        return retVal;
    }
    
    Object onValidateFromOpenVault(String vaultIdStr) {
        UUID vaultId = UUID.fromString(vaultIdStr);
        Vault vault = vaultService.retrieveById(vaultId);
        selectedVault = vault;
        try {
            vaultService.openVault(vault, vaultPassword);
        } catch (PegasusException e) {
            if (e.getErrorCode() == PegasusErrorCode.PG302) {
                openVault.recordError("The code is incorrect for this vault");
                return openVaultZone;
            } else {
                throw e;
            }
        }
        return Boolean.TRUE;
    }
    
    Object onSuccessFromOpenVault(String vaultIdStr) {
        UUID vaultId = UUID.fromString(vaultIdStr);
        AuthenticatedMember current = memberService.getCurrent();
        Vault defaultVault = current.getMember().getDefaultVault();
        if (defaultVault.getId().equals(vaultId)) {
            return MemberIndex.class;
        }
        return openVaultZone;
    }

    Object onActionFromShowVaultOpenForm(final String vaultId) {
        selectedVault = vaultService.retrieveById(UUID.fromString(vaultId));
        return openVaultZone.getBody();
    }
    
    Object onActionFromCloseVault(final String vaultId) {
        vaultService.closeVault(UUID.fromString(vaultId));
        return openVaultZone.getBody();
    }
    
    public boolean isSelectedVault() {
        return selectedVault.getId().equals(loopVault.getId());
    }
    
    public String getVaultStyleClass() {
        String cssClass = "vault locked";
        if (isVaultOpen()) {
            cssClass = "vault open";
        }
        return cssClass;
    }
    
    
    public List<Inbox> getInboxList() {
        return inboxService.retrieveForKeySafe(loopVault);
    }
    
    public List<Deposit> getDepositList() {
        return inboxService.retrieveDeposits(loopInbox);
    }
    
    public List<Vault> getVaultList() {
        return vaultService.retrieveForUser();
    }
    
    public List<Associate> getAssociateList() {
        return organizationService.retrieveAssociates(loopVault);
    }
    
    public List<Invitation> getInvistationList() {
        return invitationService.retrieveCurrent(loopVault);
    }
    
    public boolean isVaultOpen() {
        return vaultService.isOpen(loopVault);
    }
    
    public String getInboxName() {
        if (loopInbox.getName() != null) {
            return loopInbox.getName();
        }
        return loopInbox.getToken().getPath();
    }
    
    
    public Object[] getInboxDispatchContext() {
        return new Object[] { loopVault.getSlug(), MakeKeyUtils.newKey() };
    }
    
    public Object[] getSentDispatchContext() {
        return new Object[] { loopVault.getSlug(), "today" };
    }
//    public Object[] getOrganizationDispatchContext() {
//        return new Object[] { loopAssociate.getOrganization().getToken().getPath(), loopAssociate.getD };
//    }
}
