/**
 * 
 */
package org.brekka.pegasus.web.pages.org;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.Enlistment;
import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.services.DivisionService;
import org.brekka.pegasus.core.services.InboxService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.OrganizationService;
import org.brekka.pegasus.web.pages.member.MemberIndex;
import org.brekka.pegasus.web.support.MakeKeyUtils;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class OrgIndex {

    @Inject
    private MemberService memberService;
    
    @Inject
    private DivisionService divisionService;
    
    @Inject
    private OrganizationService organizationService;
    
    @Inject
    private InboxService inboxService;
    
    @Property
    private Organization organization;
    
    @Property
    private Enlistment loopDivision;
    
    @Property
    private Inbox loopInbox;
    
    public void init(Organization organization) {
        this.organization = organization;
    }
    
    Object onActivate(String token) {
        AuthenticatedMember authenticatedMember = memberService.getCurrent();
        if (authenticatedMember.getActiveActor() instanceof Associate == false) {
            return MemberIndex.class;
        }
        this.organization = organizationService.retrieveByToken(token);
        return Boolean.TRUE;
    }
    
    String onPassivate() {
        return organization.getToken().getPath();
    }
    
    public List<Enlistment> getDivisions() {
        return divisionService.retrieveCurrentDivisions();
    }
    
    public List<Inbox> getInboxList() {
        return inboxService.retrieveForDivision(loopDivision.getDivision());
    }
    
    public String getInboxName() {
        // TODO
        return loopInbox.getToken().getPath();
    }
    
    public Object[] getDispatchContext() {
        return ArrayUtils.add(getDivisionContext(), MakeKeyUtils.newKey());
    }
    
    public Object[] getSentDispatchContext() {
        return ArrayUtils.add(getDivisionContext(), "today");
    }
    
    public Object[] getDivisionContext() {
        return new Object[] { organization.getToken().getPath(), 
                loopDivision.getDivision().getSlug()  };
    }
    
    
}
