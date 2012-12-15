/**
 * 
 */
package org.brekka.pegasus.web.pages.org;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.OrganizationService;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class ActivateOrg {
    @InjectPage
    private OrgIndex orgIndex;
    
    @Inject
    private OrganizationService organizationService;
    
    @Inject
    private MemberService memberService;
    

    Object onActivate(String orgToken) {
        Organization organization = organizationService.retrieveByToken(orgToken, false);
        memberService.activateOrganization(organization);
        orgIndex.init(organization);
        return orgIndex;
    }
}
