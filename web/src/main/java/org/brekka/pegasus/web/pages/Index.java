/**
 * 
 */
package org.brekka.pegasus.web.pages;

import org.brekka.pegasus.web.pages.direct.MakeDirect;


/**
 * @author Andrew Taylor
 *
 */
public class Index {
    
    Object onActivate() {
        return MakeDirect.class;
    }
}
