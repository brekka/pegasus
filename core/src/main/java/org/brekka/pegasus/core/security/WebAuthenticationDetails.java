/**
 * 
 */
package org.brekka.pegasus.core.security;

import java.io.Serializable;


/**
 * @author Andrew Taylor
 * 
 */
public class WebAuthenticationDetails implements Serializable {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -3611150592320259896L;
    
    private final String remoteAddress;
    private final String onBehalfOfAddress;
    private final String userAgent;

    public WebAuthenticationDetails(String remoteAddress, String onBehalfOfAddress, String userAgent) {
        this.remoteAddress = remoteAddress;
        this.onBehalfOfAddress = onBehalfOfAddress;
        this.userAgent = userAgent;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public String getOnBehalfOfAddress() {
        return onBehalfOfAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }
}
