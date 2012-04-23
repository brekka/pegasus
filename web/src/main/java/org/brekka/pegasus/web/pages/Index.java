/**
 * 
 */
package org.brekka.pegasus.web.pages;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tapestry5.annotations.InjectPage;


/**
 * @author Andrew Taylor
 *
 */
public class Index {
    @InjectPage
    private Make makePage;
    
    Object onActivate() {
        String makeKey = RandomStringUtils.randomAlphabetic(4);
        makePage.onActivate(makeKey);
        return makePage;
    }
}
