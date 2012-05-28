/**
 * 
 */
package org.brekka.pegasus.web.pages;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.RequestGlobals;
import org.brekka.pegasus.core.security.PegasusAuthority;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.web.pages.member.MemberIndex;
import org.brekka.pegasus.web.pages.member.SetupMember;


/**
 * @author Andrew Taylor
 *
 */
public class Index {
    
    @Inject
    private MemberService memberService;
    
    @Inject
    private RequestGlobals requestGlobals;
    
    public Object onActivate() {
        if (memberService.isNewMember()) {
            return SetupMember.class;
        }
        if (memberService.getCurrent() != null) {
            return MemberIndex.class;
        }
        return Boolean.TRUE;
    }
    
    public String getContextPath() {
        return requestGlobals.getRequest().getContextPath();
    }
    
    public boolean isDirectAllowed() {
        return memberService.hasAccess(PegasusAuthority.ANONYMOUS_TRANSFER);
    }
    
    public boolean isSignupAllowed() {
        return memberService.hasAccess(PegasusAuthority.MEMBER_SIGNUP);
    }
}
