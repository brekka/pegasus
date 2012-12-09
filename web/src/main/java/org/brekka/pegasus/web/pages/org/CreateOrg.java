/**
 * 
 */
package org.brekka.pegasus.web.pages.org;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.model.DivisionAssociate;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.OrganizationService;
import org.brekka.xml.pegasus.v2.model.OrganizationDocument;

/**
 * Create a new Organization
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class CreateOrg {
    @InjectPage
    private OrgIndex orgIndex;
    
    
    @Inject
    private OrganizationService organizationService;
    
    
    @Inject
    private MemberService memberService;
    
    @Property
    private String name;
    
    @Property
    private String orgToken;
    
    @Property
    private String domainName;
    
    @Property
    private String orgOwnerEmail;
    
    Object onActivate() {
        return Boolean.TRUE;
    }
    
    Object onSuccess() {
        Member member = memberService.getCurrent().getMember();
        OrganizationDocument orgDoc = OrganizationDocument.Factory.newInstance();
        orgDoc.addNewOrganization();
        DivisionAssociate divisionAssociate = organizationService.createOrganizationDivisionAssociate(
                name, orgToken, domainName, orgOwnerEmail, orgDoc, member.getDefaultVault());
        orgIndex.init(divisionAssociate.getDivision().getOrganization());
        return orgIndex;
    }
}
