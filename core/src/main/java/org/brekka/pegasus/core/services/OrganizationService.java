/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.List;
import java.util.UUID;

import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.DivisionAssociate;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.model.XmlEntity;
import org.brekka.xml.pegasus.v2.model.OrganizationDocument;

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
     * @param idToAssign entirely optional
     * @return
     */
    DivisionAssociate createOrganization(String name, String tokenStr, String domainNameStr, 
            String ownerEmail, OrganizationDocument organizationDocument, Vault connectedTo, UUID idToAssign);

    void updateOrganizationDetails(UUID orgId, XmlEntity<OrganizationDocument> orgXml);
    
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
     * @param organization
     * @param member
     * @return
     */
    Associate retrieveAssociate(Organization organization, Member member);

    /**
     * @param primaryCompanyId
     * @return
     */
    Organization retrieveById(UUID orgId, boolean releaseXml);

    /**
     * Does an organization with this ID exist?
     * @param orgId
     * @return
     */
    boolean exists(UUID orgId);

}
