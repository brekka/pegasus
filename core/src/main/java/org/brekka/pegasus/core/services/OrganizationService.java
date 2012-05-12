/**
 * 
 */
package org.brekka.pegasus.core.services;

import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Vault;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface OrganizationService {

    /**
     * @param name
     * @param tokenStr
     * @param domainNameStr
     * @param ownerEmail
     * @param owner
     * @param toMemberVault
     * @return
     */
    Organization createOrganization(String name, String tokenStr, String domainNameStr, 
            String ownerEmail, Member owner, Vault toMemberVault);

}
