/**
 * 
 */
package org.brekka.pegasus.web.filter;

import static org.brekka.pegasus.core.security.PegasusAuthority.*;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.brekka.pegasus.core.security.AnonymousAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Identifies when a user is attempting to access a resource which requires a proper login. 
 * 
 * An alternative would be to override the {@link AuthenticationTrustResolver} to indentify our custom
 * anonymous type but that would require the custom bean definition of a lot of spring security classes.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class SwitchAuthenticationFilter extends GenericFilterBean {

    private final FilterInvocationSecurityMetadataSource securityMetadataSource;
    
    private final HttpSessionRequestCache cache = new HttpSessionRequestCache();
    
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    
    
    @Autowired
    public SwitchAuthenticationFilter(FilterSecurityInterceptor filterSecurityInterceptor) {
        this.securityMetadataSource = filterSecurityInterceptor.getSecurityMetadataSource();
    }



    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
            ServletException {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            // Check whether we need a login.
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) res;
            FilterInvocation fi = new FilterInvocation(request, response, chain);
            boolean anon = false;
            boolean firewalled = false;
            Collection<ConfigAttribute> attributes = securityMetadataSource.getAttributes(fi);
            for (ConfigAttribute configAttribute : attributes) {
                String authority = configAttribute.getAttribute();
                if (ANONYMOUS_TRANSFER.getAuthority().equals(authority)) {
                    // Requires the anon transfer authority, does the user have it?
                    firewalled = !authentication.getAuthorities().contains(ANONYMOUS_TRANSFER);
                    anon = true;
                    break;
                }
                if (MEMBER_SIGNUP.getAuthority().equals(authority)) {
                    // Can the user signup?
                    firewalled = !authentication.getAuthorities().contains(MEMBER_SIGNUP);
                    anon = true;
                    break;
                }
                if (ANONYMOUS.getAuthority().equals(authority)) {
                    anon = true;
                    break;
                }
            }
            if (firewalled) {
                redirectStrategy.sendRedirect(request, response, "/denied");
            } else if (anon) {
                chain.doFilter(req, res);
            } else {
                // Need to login
                cache.saveRequest(request, response);
                redirectStrategy.sendRedirect(request, response, "/login");
            }
        } else {
            chain.doFilter(req, res);
        }
    }

    
}
