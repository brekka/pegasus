/**
 * 
 */
package org.brekka.pegasus.web.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.brekka.pegasus.core.model.Firewall;
import org.brekka.pegasus.core.security.WebAuthenticationDetails;
import org.brekka.pegasus.core.services.FirewallService;
import org.brekka.pegasus.web.security.WebAuthenticationDetailsSource;
import org.brekka.pegasus.web.support.Configuration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.filter.GenericFilterBean;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class SpecialAnonymousAuthenticationFilter extends GenericFilterBean implements InitializingBean {

    public static final GrantedAuthority ANONYOUS_TRANSFER = new SimpleGrantedAuthority("ROLE_ANONYMOUS_TRANSFER");
    public static final GrantedAuthority MEMBER_SIGNUP = new SimpleGrantedAuthority("ROLE_MEMBER_SIGNUP");
    public static final GrantedAuthority ANONYMOUS = new SimpleGrantedAuthority("ROLE_ANONYMOUS");
    
    private final WebAuthenticationDetailsSource authenticationDetailsSource;
    
    private final FirewallService firewallService;
    
    private final Configuration configuration;
    
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
    
    private Firewall anonymousAccess;
    
    private Firewall memberSignup;

    @Autowired
    public SpecialAnonymousAuthenticationFilter(
            FirewallService firewallService, 
            WebAuthenticationDetailsSource authenticationDetailsSource, 
            Configuration configuration, 
            SessionAuthenticationStrategy sessionAuthenticationStrategy) {
        this.firewallService = firewallService;
        this.authenticationDetailsSource = authenticationDetailsSource;
        this.configuration = configuration;
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.web.filter.GenericFilterBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws ServletException {
        anonymousAccess = firewallService.retrieveConfiguredFirewall(configuration.getRoot().getAnonymousTransfer().getFirewall());
        memberSignup = firewallService.retrieveConfiguredFirewall(configuration.getRoot().getMemberSignup().getFirewall());
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
            ServletException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) res;
            Authentication authentication = createAuthentication(request);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            sessionAuthenticationStrategy.onAuthentication(authentication, request, response);

            if (logger.isDebugEnabled()) {
                logger.debug("Populated SecurityContextHolder with anonymous token: '"
                    + SecurityContextHolder.getContext().getAuthentication() + "'");
            }
        } 
        chain.doFilter(req, res);
    }
    
    protected Authentication createAuthentication(HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        List<GrantedAuthority> granted = new ArrayList<>(3);
        granted.add(ANONYMOUS);
        if (firewallService.isAccessAllowed(anonymousAccess, ipAddress)) {
            // Can send files anonymously
            granted.add(ANONYOUS_TRANSFER);
        }
        if (firewallService.isAccessAllowed(memberSignup, ipAddress)) {
            // Can sign up
            granted.add(MEMBER_SIGNUP);
        }
        AnonymousAuthenticationToken auth = new AnonymousAuthenticationToken("anonymous",  "anonymousUser", granted);
        WebAuthenticationDetails details = authenticationDetailsSource.buildDetails(request);
        auth.setDetails(details);
        return auth;
    }
}
