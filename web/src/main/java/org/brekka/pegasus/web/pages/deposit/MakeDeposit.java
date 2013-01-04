/**
 * 
 */
package org.brekka.pegasus.web.pages.deposit;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.RequestGlobals;
import org.brekka.paveway.core.model.UploadedFiles;
import org.brekka.paveway.web.session.UploadsContext;
import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.core.model.PegasusAllocationDisposition;
import org.brekka.pegasus.core.services.InboxService;
import org.brekka.pegasus.web.base.AbstractMakePage;
import org.brekka.pegasus.web.support.MakeKeyUtils;
import org.brekka.xml.pegasus.v2.model.DetailsType;
import org.joda.time.DateTime;

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
        this.inbox = inboxService.retrieveForToken(inboxToken);
        String makeKey = MakeKeyUtils.newKey();
        HttpServletRequest req = requestGlobals.getHTTPServletRequest();
        UploadsContext bundleMakerContext = UploadsContext.get(req, true);
        bundleMakerContext.get(makeKey);
        init(inbox, makeKey);
        return super.activate();
    }
    
    Object onActivate(String inboxToken, String makeKey) {
        this.inbox = inboxService.retrieveForToken(inboxToken);
        this.makeKey = makeKey;
        return super.activate(makeKey);
    }
    
    public void init(Inbox inbox, String makeKey) {
        super.init(makeKey);
        this.inbox = inbox;
    }

    String[] onPassivate() {
        return new String[] { inbox.getToken().getPath(), super.passivate() };
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.web.base.AbstractMakePage#onSuccess(java.util.List, java.lang.String)
     */
    @Override
    protected Object onSuccess(String comment, UploadedFiles files) {
        DetailsType detailsType = DetailsType.Factory.newInstance();
        detailsType.setReference(reference);
        detailsType.setComment(comment);
        // TODO Expiry for deposits
        DateTime expires = new DateTime().plusDays(14);
        Allocation allocation = inboxService.createDeposit(inbox, PegasusAllocationDisposition.TRANSFER, detailsType, expires, files);
        depositDonePage.init(makeKey, allocation);
        return depositDonePage;
    }
}
