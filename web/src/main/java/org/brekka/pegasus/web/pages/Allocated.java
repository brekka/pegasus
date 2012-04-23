/**
 * 
 */
package org.brekka.pegasus.web.pages;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Persist;
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
    
    
    void onActivate(String makeKey) {
        this.makeKey = makeKey;
        
        HttpServletRequest req = requestGlobals.getHTTPServletRequest();
        BundleMakerContext bundleMakerContext = BundleMakerContext.get(req, true);
        BundleMaker bundleMaker = bundleMakerContext.get(makeKey);
        transferKey = bundleMaker.getTransferKey();
    }
    
    String onPassivate() {
        return makeKey;
    }
    
    public String getUnlockLink() {
        return configuration.getFetchBase() + "/" + transferKey.getSlug(); 
    }
    
    public String getDirectLink() {
        String path;
        if (transferKey.getFileName() != null) {
            path = transferKey.getCode() + "/" + transferKey.getSlug() + "/" + transferKey.getFileName();
        } else {
            path = transferKey.getCode() + "/" + transferKey.getSlug() + ".zip";
        }
        return configuration.getFetchBase() + "/" + path;
    }
}
