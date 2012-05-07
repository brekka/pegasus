/**
 * 
 */
package org.brekka.pegasus.web.session;

import java.util.List;

import org.brekka.pegasus.core.services.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.session.SessionDestroyedEvent;

/**
 * Handle session logouts using Spring security events
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class SessionTerminationListener implements ApplicationListener<SessionDestroyedEvent> {
    
    @Autowired
    private MemberService memberService;
    
    @Override
    public void onApplicationEvent(SessionDestroyedEvent event) {
        List<SecurityContext> securityContexts = event.getSecurityContexts();
        for (SecurityContext securityContext : securityContexts) {
            memberService.logout(securityContext);
        }
    }
}
