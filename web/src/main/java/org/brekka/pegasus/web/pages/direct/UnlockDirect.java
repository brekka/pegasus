/**
 * 
 */
package org.brekka.pegasus.web.pages.direct;

import javax.inject.Inject;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.brekka.pegasus.core.services.AnonymousService;
import org.brekka.pegasus.web.support.Bundles;
import org.brekka.xml.pegasus.v1.model.BundleType;

/**
 * @author Andrew Taylor
 *
 */
public class UnlockDirect {
    public static final String PATH = "direct/unlock";
    
    @InjectPage
    private FetchDirect fetchPage;
    
    @Inject
    private AnonymousService anonymousService;
    
    @SessionAttribute("bundles")
    private Bundles bundles;
    
    @Property
    private String token;

    @Property
    private String code;
    
    void onActivate(String token) {
        this.token = token;
        if (bundles == null) {
            bundles = new Bundles();
        }
    }
    
    String onPassivate() {
        return token;
    }
    
    Object onSuccess() {
        BundleType bundle = anonymousService.unlock(token, code, null);
        bundles.add(token, bundle);
        fetchPage.init(token);
        return fetchPage;
    }
}
