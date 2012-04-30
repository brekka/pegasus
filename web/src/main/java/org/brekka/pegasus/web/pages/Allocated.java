/**
 * 
 */
package org.brekka.pegasus.web.pages;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.RequestGlobals;
import org.brekka.pegasus.core.model.TransferKey;
import org.brekka.pegasus.web.session.BundleMaker;
import org.brekka.pegasus.web.session.BundleMakerContext;
import org.brekka.pegasus.web.support.Configuration;

/**
 * @author Andrew Taylor
 *
 */
public class Allocated {
    
    @Inject
    private Configuration configuration;
    
    @Property
    private String makeKey;
    
    @Inject
    private RequestGlobals requestGlobals;
    
    @Property
    private TransferKey transferKey;
    
    @Inject
    private AlertManager alertManager;
    
    
    Object onActivate(String makeKey) {
        this.makeKey = makeKey;
        
        HttpServletRequest req = requestGlobals.getHTTPServletRequest();
        BundleMakerContext bundleMakerContext = BundleMakerContext.get(req, true);
        if (bundleMakerContext.contains(makeKey)) {
            BundleMaker bundleMaker = bundleMakerContext.get(makeKey);
            transferKey = bundleMaker.getTransferKey();
            return Boolean.TRUE;
        }
        alertManager.alert(Duration.SINGLE, Severity.WARN, "Details of the requested upload are no longer available.");
        return Index.class;
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
