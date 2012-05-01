/**
 * 
 */
package org.brekka.pegasus.web.pages.direct;

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
public class FetchDirect {
    
    @InjectPage
    private UnlockDirect unlockPage;

    @SessionAttribute("bundles")
    private Bundles bundles;
    
    @Property
    private String token;
    
    @Property
    private BundleType bundle;
    
    @Property
    private FileType file;
    
    
    Object onActivate(String token) {
        this.token = token;
        
        if (bundles == null) {
            unlockPage.onActivate(token);
            return unlockPage;
        }
        
        bundle = bundles.get(token);
        if (bundle == null) {
            unlockPage.onActivate(token);
            return unlockPage;
        }
        return Boolean.TRUE;
    }
    
    void init(String token) {
        this.token = token;
    }
    
    String onPassivate() {
        return token;
    }
    
    public String[] getFileContext() {
        return new String[]{ file.getUUID(), file.getName() };
    }
}
