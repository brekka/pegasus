/**
 * 
 */
package org.brekka.pegasus.web.pages;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.services.RequestGlobals;
import org.brekka.pegasus.core.services.AnonymousService;
import org.brekka.pegasus.web.support.Bundles;
import org.brekka.xml.pegasus.v1.model.BundleType;

/**
 * @author Andrew Taylor
 *
 */
public class Unlock {
    
    @InjectPage
    private Fetch fetchPage;
    
    @Inject
    private AnonymousService anonymousService;
    
    @Inject
    private RequestGlobals requestGlobals;
    
    @SessionAttribute("bundles")
    private Bundles bundles;
    
    @Property
    private String slug;

    @Property
    private String code;
    
    void onActivate(String slug) {
        this.slug = slug;
        if (bundles == null) {
            bundles = new Bundles();
        }
    }
    
    String onPassivate() {
        return slug;
    }
    
    Object onSuccess() {
        HttpServletRequest req = requestGlobals.getHTTPServletRequest();
        String userAgent = req.getHeader("User-Agent");
        String onBehalfOfAddress = req.getHeader("X-Forwarded-For");
        String remoteAddr = req.getRemoteAddr();
        
        BundleType bundle = anonymousService.unlock(slug, code, null, remoteAddr, onBehalfOfAddress, userAgent);
        bundles.add(slug, bundle);
        fetchPage.onActivate(slug);
        return fetchPage;
    }
}
