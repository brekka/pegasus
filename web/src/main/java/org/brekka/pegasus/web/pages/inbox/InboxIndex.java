/**
 * 
 */
package org.brekka.pegasus.web.pages.inbox;

import java.util.List;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.model.Deposit;
import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.core.services.InboxService;
import org.brekka.pegasus.web.support.Bundles;
import org.brekka.pegasus.web.support.Configuration;
import org.brekka.xml.pegasus.v1.model.BundleType;
import org.brekka.xml.pegasus.v1.model.FileType;

/**
 * List the deposits in the index
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class InboxIndex {
    @Inject
    private Configuration configuration;
    
    @Inject
    private InboxService inboxService;
    
    @SessionAttribute("bundles")
    private Bundles bundles;
    
    @Property
    private Inbox inbox;
    
    @Property
    private Deposit loopDeposit;
    
    @Property
    private FileType loopFile;
    
    Object onActivate(String token) {
        inbox = inboxService.retrieveForToken(token);
        return Boolean.TRUE;
    }
    
    String onPassivate() {
        return inbox.getToken().getPath();
    }
    
    public List<Deposit> getDeposits() {
        return inboxService.retrieveDeposits(inbox);
    }
    
    public String getInboxLink() {
        return configuration.getFetchBase() + "/" + inbox.getToken().getPath(); 
    }
    
    public String[] getFileContext() {
        return new String[]{ loopFile.getUUID(), loopFile.getName() };
    }
    
    public BundleType getBundle() {
        String bundleId = loopDeposit.getBundle().getId().toString();
        if (!bundles.contains(bundleId)) {
            BundleType bundle = inboxService.unlock(loopDeposit);
            bundles.add(bundleId, bundle);
        }
        return bundles.get(bundleId);
    }
}
