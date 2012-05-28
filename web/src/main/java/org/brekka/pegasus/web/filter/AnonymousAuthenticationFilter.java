/**
 * 
 */
package org.brekka.pegasus.web.filter;

import static org.brekka.pegasus.core.security.PegasusAuthority.ANONYMOUS;
import static org.brekka.pegasus.core.security.PegasusAuthority.ANONYMOUS_TRANSFER;
import static org.brekka.pegasus.core.security.PegasusAuthority.MEMBER_SIGNUP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.brekka.pegasus.core.model.Firewall;
import org.brekka.pegasus.core.security.WebAuthenticationDetails;
import org.brekka.pegasus.core.services.FirewallService;
import org.brekka.pegasus.web.security.AnonymousAuthenticationToken;
import org.brekka.pegasus.web.security.WebAuthenticationDetailsSource;
import org.brekka.pegasus.web.support.Configuration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class AnonymousAuthenticationFilter extends GenericFilterBean implements InitializingBean {

    private final WebAuthenticationDetailsSource authenticationDetailsSource;
    
    private final FirewallService firewallService;
    
    private final Configuration configuration;
    
    private Firewall anonymousAccess;
    
    private Firewall memberSignup;

    @Autowired
    public AnonymousAuthenticationFilter(
            FirewallService firewallService, 
            WebAuthenticationDetailsSource authenticationDetailsSource, 
            Configuration configuration) {
        this.firewallService = firewallService;
        this.authenticationDetailsSource = authenticationDetailsSource;
        this.configuration = configuration;
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
//            HttpServletResponse response = (HttpServletResponse) res;
            Authentication authentication = createAuthentication(request);
            SecurityContextHolder.getContext().setAuthentication(authentication);
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
            granted.add(ANONYMOUS_TRANSFER);
        }
        if (firewallService.isAccessAllowed(memberSignup, ipAddress)) {
            // Can sign up
            granted.add(MEMBER_SIGNUP);
        }
        AnonymousAuthenticationToken auth = new AnonymousAuthenticationToken("anonymousUser", granted);
        WebAuthenticationDetails details = authenticationDetailsSource.buildDetails(request);
        auth.setDetails(details);
        return auth;
    }
}
