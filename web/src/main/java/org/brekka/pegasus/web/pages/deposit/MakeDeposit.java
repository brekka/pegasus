/**
 * 
 */
package org.brekka.pegasus.web.pages.deposit;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.RequestGlobals;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.core.services.InboxService;
import org.brekka.pegasus.web.base.AbstractMakePage;
import org.brekka.pegasus.web.pages.Index;
import org.brekka.pegasus.web.pages.NoSession;
import org.brekka.pegasus.web.session.AllocationMaker;
import org.brekka.pegasus.web.session.AllocationMakerContext;
import org.brekka.pegasus.web.support.MakeKeyUtils;
import org.brekka.xml.pegasus.v2.model.DetailsType;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class MakeDeposit extends AbstractMakePage {
    public static final String PATH = "deposit/make";
    
    @Inject
    private RequestGlobals requestGlobals;
    
    @Inject
    private InboxService inboxService;

    @InjectPage
    private DepositDone depositDonePage;
    
    @Property
    private Inbox inbox;
    
    @Property
    private String reference;
    
    
    /**
     * Redirect assigning a new key
     * @return
     */
    Object onActivate(String inboxToken) {
        Inbox inbox = inboxService.retrieveForToken(inboxToken);
        String makeKey = MakeKeyUtils.newKey();
        HttpServletRequest req = requestGlobals.getHTTPServletRequest();
        AllocationMakerContext bundleMakerContext = AllocationMakerContext.get(req, true);
        bundleMakerContext.get(makeKey, inbox);
        init(inbox, makeKey);
        return this;
    }
    
    Object onActivate(String inboxToken, String makeKey) {
        this.inbox = inboxService.retrieveForToken(inboxToken);
        this.makeKey = makeKey;
        HttpServletRequest req = requestGlobals.getHTTPServletRequest();
        AllocationMakerContext bundleMakerContext = AllocationMakerContext.get(req, false);
        if (bundleMakerContext == null) {
            return NoSession.class;
        }
        if (!bundleMakerContext.contains(makeKey)) {
            alertManager.alert(Duration.SINGLE, Severity.WARN, "Sorry, but the uploaded files are no longer available. Please try again.");
            return onActivate(inboxToken);
        }
        AllocationMaker bundleMaker = bundleMakerContext.get(makeKey);
        if (bundleMaker.isDone()) {
            return Index.class;
        }
        files = bundleMaker.previewCompleted();
        
        return Boolean.TRUE;
    }
    
    public void init(Inbox inbox, String makeKey) {
        super.init(makeKey);
        this.inbox = inbox;
    }

    String[] onPassivate() {
        return new String[] { inbox.getToken().getPath(), makeKey };
    }

    Object onSuccess() throws Exception {
        Object retVal;
        HttpServletRequest req = requestGlobals.getHTTPServletRequest();
        AllocationMakerContext bundleMakerContext = AllocationMakerContext.get(req, true);
        AllocationMaker bundleMaker = bundleMakerContext.get(makeKey);
        if (!bundleMaker.isDone()) {
            List<FileBuilder> fileBuilderList = processFiles(bundleMaker);
            DetailsType detailsType = DetailsType.Factory.newInstance();
            detailsType.setReference(reference);
            detailsType.setComment(comment);
            Allocation transferKey = inboxService.createDeposit(inbox, detailsType, fileBuilderList);
            bundleMaker.setAllocation(transferKey);
            depositDonePage.init(makeKey);
            retVal = depositDonePage;
        } else {
            // TODO bad handling
            throw new IllegalStateException();
        }
        return retVal;
    }
}
