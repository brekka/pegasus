/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.brekka.pegasus.core.dao.AssociateDAO;
import org.brekka.pegasus.core.dao.DivisionAssociateDAO;
import org.brekka.pegasus.core.dao.DivisionDAO;
import org.brekka.pegasus.core.dao.OrganizationDAO;
import org.brekka.pegasus.core.model.ActorStatus;
import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.Enlistment;
import org.brekka.pegasus.core.model.DomainName;
import org.brekka.pegasus.core.model.EMailAddress;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Token;
import org.brekka.pegasus.core.model.TokenType;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.model.XmlEntity;
import org.brekka.pegasus.core.services.DivisionService;
import org.brekka.pegasus.core.services.EMailAddressService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.OrganizationService;
import org.brekka.pegasus.core.services.TokenService;
import org.brekka.pegasus.core.services.VaultService;
import org.brekka.pegasus.core.services.XmlEntityService;
import org.brekka.xml.pegasus.v2.model.OrganizationDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Service
@Transactional
public class OrganizationServiceImpl implements OrganizationService {

    @Autowired
    private DivisionDAO divisionDAO;
    
    @Autowired
    private OrganizationDAO organizationDAO;
    
    @Autowired
    private AssociateDAO associateDAO;
    
    @Autowired
    private DivisionAssociateDAO divisionAssociateDAO;
    
    @Autowired
    private EMailAddressService eMailAddressService;
    
    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private VaultService vaultService;
    
    @Autowired
    private MemberService memberService;
    
    @Autowired
    private XmlEntityService xmlEntityService;
    
    @Autowired
    private DivisionService divisionService;
    
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Organization createOrganization(String name, String tokenStr, String domainNameStr, UUID idToAssign) {
        Organization organization = new Organization();
        organization.setId(idToAssign);
        organization.setName(name);
        
        if (StringUtils.isNotBlank(domainNameStr)) {
            DomainName domainName = eMailAddressService.toDomainName(domainNameStr);
            organization.setPrimaryDomainName(domainName);
        }
        Token token = tokenService.createToken(tokenStr, TokenType.ORG);
        organization.setToken(token);
        
        organizationDAO.create(organization);
        return organization;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.OrganizationService#createOrganizationAndDivisionAssociate(java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.brekka.xml.pegasus.v2.model.OrganizationDocument, org.brekka.pegasus.core.model.Vault)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Enlistment createOrganizationDivisionAssociate(String name, String orgToken, String domainName,
            String orgOwnerEmail, OrganizationDocument orgDoc, Vault toVault) {
        Organization organization = createOrganization(name, orgToken, domainName, null);
        Associate associate = createAssociate(organization, toVault.getOwner(), orgOwnerEmail);
        Enlistment divisionAssociate = divisionService.createRootDivision(associate, toVault, "top", "Top");
        return divisionAssociate;
    }
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Associate createAssociate(Organization organization, Member owner, String ownerEmailStr) {
        // Add current user as an associate
        Associate associate = new Associate();
        associate.setOrganization(organization);
        associate.setStatus(ActorStatus.ACTIVE);
        associate.setMember(owner);
        if (StringUtils.isNotBlank(ownerEmailStr)) {
            EMailAddress ownerEMail = eMailAddressService.createEMail(ownerEmailStr, owner, false);
            associate.setPrimaryEMailAddress(ownerEMail);
        }
        associateDAO.create(associate);
        return associate;
    }
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public XmlEntity<OrganizationDocument> updateOrganizationDetails(UUID orgId, XmlEntity<OrganizationDocument> orgXml) {
        Organization organization = organizationDAO.retrieveById(orgId);
        XmlEntity<OrganizationDocument> entity = xmlEntityService.updateEntity(orgXml, organization.getXml(), OrganizationDocument.class);
        organization.setXml(entity);
        organizationDAO.update(organization);
        return entity;
    }
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public XmlEntity<OrganizationDocument> createOrganizationDetails(UUID orgId, OrganizationDocument organizationDocument, Division<Organization> division) {
        Organization organization = organizationDAO.retrieveById(orgId);
        if (organization.getXml() != null) {
            throw new IllegalStateException(); // TODO
        }
        XmlEntity<OrganizationDocument> entity = xmlEntityService.persistEncryptedEntity(organizationDocument, division);
        organization.setXml(entity);
        organizationDAO.update(organization);
        return entity;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.OrganizationService#retrieveById(java.util.UUID)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Organization retrieveById(UUID orgId, boolean releaseXml) {
        Organization organization = organizationDAO.retrieveById(orgId);
        if (organization == null) {
            return null;
        }
        if (organization.getXml() != null && releaseXml) {
            XmlEntity<OrganizationDocument> entity = xmlEntityService.retrieveEntity(organization.getXml().getId(), OrganizationDocument.class);
            organization.setXml(entity);
        }
        return organization;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.OrganizationService#exists(java.util.UUID)
     */
    @Override
    public boolean organizationExists(UUID orgId) {
        return organizationDAO.retrieveById(orgId) != null;
    }
    
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.OrganizationService#retrieveByToken(java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Organization retrieveByToken(String tokenPath) {
        Token token = tokenService.retrieveByPath(tokenPath);
        return organizationDAO.retrieveByToken(token);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.OrganizationService#retrieveAssociate(org.brekka.pegasus.core.model.Organization, org.brekka.pegasus.core.model.Member)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Associate retrieveAssociate(Organization organization, Member member) {
        Associate associate = associateDAO.retrieveByOrgAndMember(organization, member);
        return associate;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.OrganizationService#retrieveAssociates(org.brekka.pegasus.core.model.KeySafe)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public List<Associate> retrieveAssociates(Vault vault) {
        List<Associate> asociateList = associateDAO.retrieveAssociatesInVault(vault);
        return asociateList;
    }
}
