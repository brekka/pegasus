/**
 * 
 */
package org.brekka.pegasus.web.components;

import java.util.Collection;
import java.util.List;

import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.ActorStatus;
import org.brekka.pegasus.core.model.Person;
import org.brekka.pegasus.core.services.MemberService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

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
        String name = "?";
        if (user == null) {
            return null;
        }
        Member member = user.getMember();
        if (member.getStatus() == ActorStatus.NEW) {
            name = "New Member";
        } else if (member instanceof Person) {
            Person person = (Person) member;
            name = person.getFullName();
            if (name == null) {
                name = "Name locked";
            }
        }
        return name;
    }
    
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities();
        }
        return null;
    }
}
