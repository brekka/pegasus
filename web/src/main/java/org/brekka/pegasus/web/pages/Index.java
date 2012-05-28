/**
 * 
 */
package org.brekka.pegasus.web.pages;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.RequestGlobals;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.web.filter.AnonymousAuthenticationFilter;


/**
 * @author Andrew Taylor
 *
 */
public class Index {
    
    @Inject
    private MemberService memberService;
    
    @Inject
    private RequestGlobals requestGlobals;
    
    public String getContextPath() {
        return requestGlobals.getRequest().getContextPath();
    }
    
    public boolean isDirectAllowed() {
        return memberService.hasAccess(AnonymousAuthenticationFilter.ANONYMOUS_TRANSFER);
    }
    
    public boolean isSignupAllowed() {
        return memberService.hasAccess(AnonymousAuthenticationFilter.MEMBER_SIGNUP);
    }
}
