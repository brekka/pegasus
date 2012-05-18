/**
 * 
 */
package org.brekka.pegasus.web.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.brekka.pegasus.web.security.AnonymousAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.web.filter.GenericFilterBean;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
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
            Collection<ConfigAttribute> attributes = securityMetadataSource.getAttributes(fi);
            for (ConfigAttribute configAttribute : attributes) {
                if (SpecialAnonymousAuthenticationFilter.ANONYMOUS.getAuthority().equals(configAttribute.getAttribute())) {
                    anon = true;
                    break;
                }
            }
            if (!anon) {
                // Need to login
                cache.saveRequest(request, response);
                redirectStrategy.sendRedirect(request, response, "/login");
            } else {
                chain.doFilter(req, res);
            }
        } else {
            chain.doFilter(req, res);
        }
    }

    
}
