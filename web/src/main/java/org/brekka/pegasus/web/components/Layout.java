/**
 * 
 */
package org.brekka.pegasus.web.components;

import java.util.Collection;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.ActorStatus;
import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.Member;
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
    
    @Inject
    @Property
    @Symbol(SymbolConstants.PRODUCTION_MODE)
    private boolean productionMode;
    
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
        Actor actor = user.getActiveActor();
        if (actor == null) {
            return null;
        }
        if (actor.getStatus() == ActorStatus.NEW) {
            name = "New Member";
        } else if (actor instanceof Person) {
            Person person = (Person) actor;
            name = person.getFullName();
            if (name == null) {
                name = "Name locked";
            }
        } else if (actor instanceof Associate) {
            Associate associate = (Associate) actor;
            Member member = associate.getMember();
            if (member instanceof Person) {
                Person person = (Person) member;
                name = person.getFullName();
                if (name == null) {
                    name = "Name locked";
                }
            } else {
                name = "Robot";
            }
            String orgName = associate.getOrganization().getName();
            name = name + " @ " + orgName;
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
