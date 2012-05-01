/**
 * 
 */
package org.brekka.pegasus.web.pages.member;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.annotations.Inject;
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
    

    
    Object onActivate() {
        Object retVal = Boolean.TRUE;
        if (memberService.isNewMember()) {
            retVal = SetupMember.class;
        }
        if (bundles == null) {
            bundles = new Bundles();
        }
        return retVal;
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
        return vaultService.isOpen(loopDeposit.getVault());
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
}
