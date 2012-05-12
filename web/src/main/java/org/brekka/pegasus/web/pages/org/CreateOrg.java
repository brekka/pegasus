/**
 * 
 */
package org.brekka.pegasus.web.pages.org;

import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.OrganizationService;
import org.brekka.pegasus.web.support.VaultEncoder;
import org.brekka.pegasus.web.support.VaultSelectModelBuilder;

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
    private VaultEncoder vaultEncoder;
    
    @Inject
    private VaultSelectModelBuilder vaultSelectModelBuilder;
    
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
    
    @Property
    private Vault selectedVault;
    
    Object onActivate() {
        selectedVault = memberService.getCurrent().getMember().getDefaultVault();
        return Boolean.TRUE;
    }
    
    Object onSuccess() {
        // TODO be able to select this
        Member member = memberService.getCurrent().getMember();
        Organization organization = organizationService.createOrganization(
                name, orgToken, domainName, orgOwnerEmail, member, selectedVault);
        orgIndex.init(organization.getToken().getPath());
        return orgIndex;
    }
    
    
    public SelectModel getVaultSelectModel() {
        return vaultSelectModelBuilder.getCurrent();
    }
    
    public VaultEncoder getVaultEncoder() {
        return vaultEncoder;
    }
}
