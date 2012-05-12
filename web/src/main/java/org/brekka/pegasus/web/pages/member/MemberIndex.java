/**
 * 
 */
package org.brekka.pegasus.web.pages.member;

import java.util.List;
import java.util.UUID;

import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.Deposit;
import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.InboxService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.VaultService;
import org.brekka.pegasus.web.support.Bundles;
import org.brekka.pegasus.web.support.Configuration;
import org.brekka.xml.pegasus.v1.model.BundleType;
import org.brekka.xml.pegasus.v1.model.FileType;

/**
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class MemberIndex {
    
    @Inject
    private Configuration configuration;
    
    @Inject
    private MemberService memberService;
    
    @Inject
    private InboxService inboxService;
    
    @Inject
    private VaultService vaultService;
    
    @InjectComponent
    private Zone openVaultZone;
    
    @Property
    private Inbox loopInbox;
    
    @Property
    private Deposit loopDeposit;
    
    @Property
    private Vault loopVault;
    
    @Property
    private FileType loopFile;
    
    @SessionAttribute("bundles")
    private Bundles bundles;
    
    @Property
    private Vault selectedVault;
    
    @Property
    private String vaultPassword;
    
    Object onActivate() {
        Object retVal = Boolean.TRUE;
        if (memberService.isNewMember()) {
            retVal = SetupMember.class;
        } else {
            if (bundles == null) {
                bundles = new Bundles();
            }
            
            AuthenticatedMember current = memberService.getCurrent();
            selectedVault = current.getMember().getDefaultVault();
        }
        return retVal;
    }
    
    Object onSuccessFromOpenVault(String vaultIdStr) {
        UUID vaultId = UUID.fromString(vaultIdStr);
        Vault vault = vaultService.retrieveById(vaultId);
        vaultService.openVault(vault, vaultPassword);
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
    
    public boolean isSelectedVault() {
        return selectedVault.getId().equals(loopVault.getId());
    }
    
    
    public List<Inbox> getInboxList() {
        return inboxService.retrieveForMember();
    }
    
    public List<Deposit> getDepositList() {
        return inboxService.retrieveDeposits(loopInbox);
    }
    
    public List<Vault> getVaultList() {
        return vaultService.retrieveForUser();
    }
    
    public boolean isVaultOpen() {
        return vaultService.isOpen(loopVault);
    }
    
    public boolean isDepositVaultOpen() {
        return vaultService.isOpen((Vault) loopDeposit.getKeySafe());
    }
    
    public BundleType getBundle() {
        String bundleId = loopDeposit.getBundle().getId().toString();
        if (!bundles.contains(bundleId)) {
            BundleType bundle = inboxService.unlock(loopDeposit);
            bundles.add(bundleId, bundle);
        }
        return bundles.get(bundleId);
    }
    
    public String[] getFileContext() {
        return new String[]{ loopFile.getUUID(), loopFile.getName() };
    }
    
    public String getInboxLink() {
        return configuration.getFetchBase() + "/" + loopInbox.getToken().getPath(); 
    }
    
    public String getInboxName() {
        if (loopInbox.getName() != null) {
            return loopInbox.getName();
        }
        return loopInbox.getToken().getPath();
    }
}
