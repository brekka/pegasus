/**
 * 
 */
package org.brekka.pegasus.web.pages;

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
public class Unlock {
    
    @InjectPage
    private Fetch fetchPage;
    
    @Inject
    private AnonymousService anonymousService;
    
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
        BundleType bundle = anonymousService.unlock(slug, code, null);
        bundles.add(slug, bundle);
        fetchPage.onActivate(slug);
        return fetchPage;
    }
}
