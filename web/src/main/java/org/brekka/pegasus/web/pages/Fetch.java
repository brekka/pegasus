/**
 * 
 */
package org.brekka.pegasus.web.pages;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.brekka.pegasus.web.support.Bundles;
import org.brekka.xml.pegasus.v1.model.BundleType;
import org.brekka.xml.pegasus.v1.model.FileType;

/**
 * @author Andrew Taylor
 *
 */
public class Fetch {
    
    @InjectPage
    private Unlock unlockPage;

    @SessionAttribute("bundles")
    private Bundles bundles;
    
    @Property
    private String slug;
    
    @Property
    private BundleType bundle;
    
    @Property
    private FileType file;
    
    
    Object onActivate(String slug) {
        this.slug = slug;
        
        if (bundles == null) {
            unlockPage.onActivate(slug);
            return unlockPage;
        }
        
        bundle = bundles.get(slug);
        if (bundle == null) {
            unlockPage.onActivate(slug);
            return unlockPage;
        }
        return Boolean.TRUE;
    }
    
    String onPassivate() {
        return slug;
    }
    
    public String[] getFileContext() {
        return new String[]{ file.getUUID(), file.getName() };
    }
}
