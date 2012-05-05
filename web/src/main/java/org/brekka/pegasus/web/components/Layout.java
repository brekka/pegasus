/**
 * 
 */
package org.brekka.pegasus.web.components;

import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.MemberStatus;
import org.brekka.pegasus.core.services.MemberService;

/**
 * @author Andrew Taylor
 *
 */
@Import(stylesheet = {
    "context:css/style.css"
})
public class Layout {

    @Inject
    private MemberService memberService;
    
    @Property
    private AuthenticatedMember user;
    
    @SetupRender
    void initialize() {
        user = memberService.getCurrent();
        if (user != null) {
            user.getProfile();
        }
    }
    
    public String getMemberName() {
        String name;
        if (user == null) {
            return null;
        }
        Member member = user.getMember();
        if (member.getStatus() == MemberStatus.NEW) {
            name = "New Member";
        } else {
            name = member.getName();
        }
        return name;
    }
}
