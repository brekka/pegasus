/**
 * 
 */
package org.brekka.pegasus.web.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.brekka.pegasus.core.security.WebAuthenticationDetails;
import org.brekka.pegasus.web.security.WebAuthenticationDetailsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.web.filter.GenericFilterBean;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class PreWebAppFilter extends GenericFilterBean {

    private final WebAuthenticationDetailsSource detailsSource;
    
    
    @Autowired
    public PreWebAppFilter(WebAuthenticationDetailsSource detailsSource) {
        this.detailsSource = detailsSource;
    }



    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getDetails() instanceof WebAuthenticationDetailsSource == false
                && authentication instanceof AbstractAuthenticationToken) {
            WebAuthenticationDetails details = detailsSource.buildDetails((HttpServletRequest) request);
            ((AbstractAuthenticationToken) authentication).setDetails(details);
        }
        
        chain.doFilter(request, response);
        
        // Performed after
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;
                if (!authentication.getAuthorities().equals(userDetails.getAuthorities())) {
                    if (authentication instanceof OpenIDAuthenticationToken) {
                        OpenIDAuthenticationToken existing = (OpenIDAuthenticationToken) authentication;
                        OpenIDAuthenticationToken token = new OpenIDAuthenticationToken(existing.getPrincipal(),
                                userDetails.getAuthorities(), existing.getIdentityUrl(), existing.getAttributes());
                        token.setDetails(existing.getDetails());
                        SecurityContextHolder.getContext().setAuthentication(token);
                    } else if (authentication instanceof RememberMeAuthenticationToken) {
                        RememberMeAuthenticationToken existing = (RememberMeAuthenticationToken) authentication;
                        RememberMeAuthenticationToken token = new RememberMeAuthenticationToken(
                                "pegasus", existing.getPrincipal(), userDetails.getAuthorities());
                        token.setDetails(existing.getDetails());
                        SecurityContextHolder.getContext().setAuthentication(token);
                    }
                }
            }
        }
    }
}
