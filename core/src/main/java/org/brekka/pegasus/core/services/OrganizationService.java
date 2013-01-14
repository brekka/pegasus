/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.EMailAddress;
import org.brekka.pegasus.core.model.Enlistment;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Partnership;
import org.brekka.pegasus.core.model.XmlEntity;
import org.brekka.xml.pegasus.v2.model.OrganizationDocument;
import org.brekka.xml.pegasus.v2.model.OrganizationType;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface OrganizationService {

    /**
     * Create a new organization, and it's root division, then assigning the specified member as the first associate.
     * 
     * @param idToAssign
     *            optionally specify the UUID to use for the organization. If null then one will be assigned.
     * @param name
     *            the name to assign the organization. Can be left as null if the name should only be included in the
     *            details document.
     * @param tokenStr
     *            the optional identification token that should be applied to this organization.
     * @param domainNameStr
     *            the optional domain name that should be associated with the organization.
     * @param details
     *            the XML details for organization. Can be left null to be set later.
     * @param owner
     *            the member who will be associated with the organization.
     * @param associateEMailStr
     *            e-mail address for the newly created associate.
     * @param protectWith
     *            the key safe that will be used to protect the private key for the root division of the newly created
     *            organization.
     * @return the enlistment between the owner as an associate and the root division.
     */
    Enlistment createOrganization(@Nullable UUID idToAssign, @Nullable String name, @Nullable String tokenStr,
            @Nullable String domainNameStr, @Nullable OrganizationType details, @Nonnull Member owner,
            @Nullable String associateEMailStr, @Nonnull KeySafe<? extends Member> protectWith);

    /**
     * Create a new organization, as a partner of some other actor. 
     * 
     * @param idToAssign
     *            optionally specify the UUID to use for the organization. If null then one will be assigned.
     * @param name
     *            the name to assign the organization. Can be left as null if the name should only be included in the
     *            details document.
     * @param tokenStr
     *            the optional identification token that should be applied to this organization.
     * @param domainNameStr
     *            the optional domain name that should be associated with the organization.
     * @param details
     *            the XML details for organization. Can be left null to be set later.
     * @param owner
     *            the member who will be associated with the organization.
     * @param owningDivision
     *            the division that will protect the private key for the root division of the newly created
     *            organization.
     * @return a new partnership that will contain a connection between the owner and the newly created organization.
     */
    <Owner extends Actor> Partnership<Owner, Organization> createOrganization(@Nullable UUID idToAssign,
            @Nullable String name, @Nullable String tokenStr, @Nullable String domainNameStr,
            @Nullable OrganizationType details, @Nonnull Owner owner, @Nonnull Division<Owner> owningDivision);

    Associate createAssociate(Organization organization, Member owner, EMailAddress eMailAddress);
    
    XmlEntity<OrganizationDocument> updateOrganizationDetails(UUID orgId, XmlEntity<OrganizationDocument> orgXml);
    
    XmlEntity<OrganizationDocument> createOrganizationDetails(UUID orgId, OrganizationType organizationDocument);
    
    
    /**
     * Retrieve the list of associates for the current user which are stored in the key safe.
     * @param loopVault
     * @return
     */
    List<Associate> retrieveAssociates(Member member);

    /**
     * @param token
     * @return
     */
    Organization retrieveByToken(String token, boolean releaseXml);

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
     * @param member
     * @param division
     * @return
     */
    Enlistment retrieveEnlistment(Member member, Division<Organization> division);

    /**
     * @param member
     */
    void deleteAssociates(Member member);
    
}
