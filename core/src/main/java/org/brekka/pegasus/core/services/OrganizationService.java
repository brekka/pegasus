/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.List;
import java.util.UUID;

import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.Enlistment;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.model.XmlEntity;
import org.brekka.xml.pegasus.v2.model.OrganizationDocument;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface OrganizationService {


    Associate createAssociate(Organization organization, Member owner, String ownerEmailStr);
    
    Organization createOrganization(String name, String tokenStr, String domainNameStr, UUID idToAssign);
    
    XmlEntity<OrganizationDocument> updateOrganizationDetails(UUID orgId, XmlEntity<OrganizationDocument> orgXml);
    
    XmlEntity<OrganizationDocument> createOrganizationDetails(UUID orgId, OrganizationDocument organizationDocument, Division<Organization> division);
    
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
    boolean organizationExists(UUID orgId);

    /**
     * @param name
     * @param orgToken
     * @param domainName
     * @param orgOwnerEmail
     * @param orgDoc
     * @param toVault
     * @return
     */
    Enlistment createOrganizationDivisionAssociate(String name, String orgToken, String domainName,
            String orgOwnerEmail, OrganizationDocument orgDoc, Vault toVault);
    
}
