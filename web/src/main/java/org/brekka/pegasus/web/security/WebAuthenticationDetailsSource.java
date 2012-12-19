/**
 * 
 */
package org.brekka.pegasus.web.security;

import javax.servlet.http.HttpServletRequest;

import org.brekka.pegasus.core.security.WebAuthenticationDetails;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.stereotype.Component;

/**
 * @author Andrew Taylor
 *
 */
@Component("authenticationDetailsSource")
public class WebAuthenticationDetailsSource implements AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails> {

    /* (non-Javadoc)
     * @see org.springframework.security.authentication.AuthenticationDetailsSource#buildDetails(java.lang.Object)
     */
    @Override
    public WebAuthenticationDetails buildDetails(HttpServletRequest req) {
        String remoteAddress = req.getRemoteAddr();
        String userAgent = req.getHeader("User-Agent");
        String onBehalfOfAddress = req.getHeader("X-Forwarded-For");
        return new WebAuthenticationDetails(remoteAddress, onBehalfOfAddress, userAgent, null);
    }
    
}
