/**
 * 
 */
package org.brekka.pegasus.web.pages;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.RequestGlobals;
import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.core.services.InboxService;
import org.brekka.pegasus.web.session.BundleMakerContext;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class PrepareDeposit {
    @InjectPage
    private Make makePage;
    
    @Inject
    private RequestGlobals requestGlobals;
    
    @Inject
    private InboxService inboxService;
    
    Object onActivate(String inboxToken) {
        Inbox inbox = inboxService.retrieveForToken(inboxToken);
        HttpServletRequest req = requestGlobals.getHTTPServletRequest();
        BundleMakerContext bundleMakerContext = BundleMakerContext.get(req, true);
        String makeKey = RandomStringUtils.randomAlphabetic(4);
        bundleMakerContext.get(makeKey, inbox);
        makePage.onActivate(makeKey);
        return makePage;
    }
}
