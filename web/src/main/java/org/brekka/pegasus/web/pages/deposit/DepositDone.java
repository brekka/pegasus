/**
 * 
 */
package org.brekka.pegasus.web.pages.deposit;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.RequestGlobals;
import org.brekka.pegasus.web.pages.Index;
import org.brekka.pegasus.web.session.BundleMaker;
import org.brekka.pegasus.web.session.BundleMakerContext;
import org.brekka.pegasus.core.services.InboxService.InboxAllocatedBundle;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class DepositDone {
    
    @Property
    private String makeKey;
    
    @Inject
    private RequestGlobals requestGlobals;
    
    @SuppressWarnings("unused")
    @Property
    private InboxAllocatedBundle transferKey;
    
    @Inject
    private AlertManager alertManager;
    
    
    Object onActivate(String makeKey) {
        this.makeKey = makeKey;
        
        HttpServletRequest req = requestGlobals.getHTTPServletRequest();
        BundleMakerContext bundleMakerContext = BundleMakerContext.get(req, true);
        if (bundleMakerContext.contains(makeKey)) {
            BundleMaker bundleMaker = bundleMakerContext.get(makeKey);
            transferKey = (InboxAllocatedBundle) bundleMaker.getTransferKey();
            return Boolean.TRUE;
        }
        alertManager.alert(Duration.SINGLE, Severity.WARN, "Details of the requested upload are no longer available.");
        return Index.class;
    }
    
    public void init(String makeKey) {
        this.makeKey = makeKey;
    }
    
    String onPassivate() {
        return makeKey;
    }
}
