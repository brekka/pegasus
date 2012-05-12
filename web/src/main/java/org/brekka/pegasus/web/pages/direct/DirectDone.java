/**
 * 
 */
package org.brekka.pegasus.web.pages.direct;

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
import org.brekka.pegasus.web.support.Configuration;

import org.brekka.pegasus.core.services.AnonymousService.AnonymousAllocatedBundle;

/**
 * @author Andrew Taylor
 *
 */
public class DirectDone {
    
    @Inject
    private Configuration configuration;
    
    @Property
    private String makeKey;
    
    @Inject
    private RequestGlobals requestGlobals;
    
    @Property
    private AnonymousAllocatedBundle transferKey;
    
    @Inject
    private AlertManager alertManager;
    
    
    Object onActivate(String makeKey) {
        this.makeKey = makeKey;
        
        HttpServletRequest req = requestGlobals.getHTTPServletRequest();
        BundleMakerContext bundleMakerContext = BundleMakerContext.get(req, true);
        if (bundleMakerContext.contains(makeKey)) {
            BundleMaker bundleMaker = bundleMakerContext.get(makeKey);
            transferKey = (AnonymousAllocatedBundle) bundleMaker.getTransferKey();
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
    
    public String getUnlockLink() {
        return configuration.getFetchBase() + "/" + transferKey.getToken(); 
    }
    
    public String getDirectLink() {
        String path;
        if (transferKey.getFileName() != null) {
            path = transferKey.getCode() + "/" + transferKey.getToken() + "/" + transferKey.getFileName();
        } else {
            path = transferKey.getCode() + "/" + transferKey.getToken() + ".zip";
        }
        return configuration.getFetchBase() + "/" + path;
    }
}
