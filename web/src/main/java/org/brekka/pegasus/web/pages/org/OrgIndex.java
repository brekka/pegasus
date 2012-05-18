/**
 * 
 */
package org.brekka.pegasus.web.pages.org;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.services.OrganizationService;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class OrgIndex {

    
    @Inject
    private OrganizationService organizationService;
    
    @Property
    private Organization organization;
    
    public void init(Organization organization) {
        this.organization = organization;
    }
    
    Object onActivate(String token) {
        this.organization = organizationService.retrieveByToken(token);
        return Boolean.TRUE;
    }
    
    String onPassivate() {
        return organization.getToken().getPath();
    }
}
