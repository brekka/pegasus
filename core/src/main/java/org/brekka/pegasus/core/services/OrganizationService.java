/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.List;

import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Vault;

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

    /**
     * Retrieve the list of associates for the current user which are stored in the key safe.
     * @param loopVault
     * @return
     */
    List<Associate> retrieveAssociates(Vault vault);

    /**
     * @param token
     * @return
     */
    Organization retrieveByToken(String token);

    /**
     * @param orgToken
     * @param divisionToken
     * @return
     */
    Division retrieveDivision(String orgToken, String divisionSlug);

    /**
     * @param organization
     * @param member
     * @return
     */
    Associate retrieveAssociate(Organization organization, Member member);

}
