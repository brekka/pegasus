/**
 * 
 */
package org.brekka.pegasus.core.services;

import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Organization;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface OrganizationService {

    /**
     * Organizations must be created by an administrator
     * 
     * @param name
     * @param tokenStr
     * @param domainNameStr
     * @param ownerEmail
     * @param owner
     * @param toMemberVault
     * @return
     */
    Organization createOrganization(String name, String tokenStr, String domainNameStr, 
            String ownerEmail, Member owner);

}
