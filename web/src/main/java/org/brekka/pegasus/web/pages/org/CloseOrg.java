/**
 * 
 */
package org.brekka.pegasus.web.pages.org;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.OrganizationService;
import org.brekka.pegasus.web.pages.member.MemberIndex;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class CloseOrg {
    
    @Inject
    private MemberService memberService;
    

    Object onActivate() {
        memberService.activateMember();
        return MemberIndex.class;
    }
}
