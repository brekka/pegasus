/**
 * 
 */
package org.brekka.pegasus.web.pages.dispatch;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.RequestGlobals;
import org.brekka.pegasus.core.model.Deposit;
import org.brekka.pegasus.web.pages.Index;
import org.brekka.pegasus.web.session.AllocationMaker;
import org.brekka.pegasus.web.session.AllocationMakerContext;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class DispatchDeposit {
    
    @Property
    private String makeKey;
    
    @Inject
    private RequestGlobals requestGlobals;
    
    @SuppressWarnings("unused")
    @Property
    private Deposit deposit;
    
    @Inject
    private AlertManager alertManager;
    
    
    Object onActivate(String makeKey) {
        this.makeKey = makeKey;
        
        HttpServletRequest req = requestGlobals.getHTTPServletRequest();
        AllocationMakerContext bundleMakerContext = AllocationMakerContext.get(req, true);
        if (bundleMakerContext.contains(makeKey)) {
            AllocationMaker bundleMaker = bundleMakerContext.get(makeKey);
            deposit = (Deposit) bundleMaker.getAllocation();
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
