/**
 * 
 */
package org.brekka.pegasus.core.security;


/**
 * @author Andrew Taylor
 * 
 */
public class WebAuthenticationDetails {

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
