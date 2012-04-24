/**
 * 
 */
package org.brekka.pegasus.web.pages;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.RequestGlobals;

/**
 * @author Andrew Taylor
 *
 */
public class Login {

    @Inject
    private RequestGlobals requestGlobals;
    
    public String getContextPath() {
        return requestGlobals.getRequest().getContextPath();
    }
}
