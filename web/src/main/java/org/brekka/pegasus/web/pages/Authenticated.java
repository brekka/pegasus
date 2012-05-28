/**
 * 
 */
package org.brekka.pegasus.web.pages;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.web.pages.member.MemberIndex;
import org.brekka.pegasus.web.pages.member.SetupMember;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class Authenticated {
    
    @Inject
    private MemberService memberService;
    
    Object onActivate() {
        if (memberService.isNewMember()) {
            // New member, go to the setup page
            return SetupMember.class;
        }
        return MemberIndex.class;
    }
}
