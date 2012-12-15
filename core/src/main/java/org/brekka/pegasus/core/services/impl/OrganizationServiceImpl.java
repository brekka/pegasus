/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.brekka.pegasus.core.dao.AssociateDAO;
import org.brekka.pegasus.core.dao.DivisionDAO;
import org.brekka.pegasus.core.dao.EnlistmentDAO;
import org.brekka.pegasus.core.dao.OrganizationDAO;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.ActorStatus;
import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.DomainName;
import org.brekka.pegasus.core.model.EMailAddress;
import org.brekka.pegasus.core.model.Enlistment;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Partnership;
import org.brekka.pegasus.core.model.Token;
import org.brekka.pegasus.core.model.TokenType;
import org.brekka.pegasus.core.model.XmlEntity;
import org.brekka.pegasus.core.services.DivisionService;
import org.brekka.pegasus.core.services.EMailAddressService;
import org.brekka.pegasus.core.services.KeySafeService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.OrganizationService;
import org.brekka.pegasus.core.services.TokenService;
import org.brekka.pegasus.core.services.VaultService;
import org.brekka.pegasus.core.services.XmlEntityService;
import org.brekka.phalanx.api.services.PhalanxService;
import org.brekka.xml.pegasus.v2.model.OrganizationDocument;
import org.brekka.xml.pegasus.v2.model.OrganizationType;
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
    private EnlistmentDAO enlistmentDAO;
    
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
    
    @Autowired
    private KeySafeService keySafeService;
    
    @Autowired
    private PhalanxService phalanxService;
    
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.OrganizationService#createOrganization(java.util.UUID, java.lang.String, java.lang.String, java.lang.String, org.brekka.xml.pegasus.v2.model.OrganizationType, org.brekka.pegasus.core.model.Member, org.brekka.pegasus.core.model.KeySafe)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Enlistment createOrganization(UUID idToAssign, String name, String tokenStr, String domainNameStr,
            OrganizationType details, Member owner, String associateEMailStr, KeySafe<Member> protectWith) {
        
        EMailAddress eMailAddress = eMailAddressService.retrieveByAddress(associateEMailStr);
        if (eMailAddress == null) {
            eMailAddress = eMailAddressService.createEMail(associateEMailStr, owner, false);
        }
        
        Organization organization = createOrganization(name, tokenStr, domainNameStr, idToAssign);
        Associate associate = createAssociate(organization, owner, eMailAddress);
        Enlistment enlistment = divisionService.createDivisionEnlistment(associate, protectWith, null, null);
        Division<Organization> globalDivision = enlistment.getDivision();
        organization.setGlobalDivision(globalDivision);
        applyDetailsXML(details, globalDivision);
        return enlistment;
    }
    

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.OrganizationService#createOrganization(java.util.UUID, java.lang.String, java.lang.String, java.lang.String, org.brekka.xml.pegasus.v2.model.OrganizationType, org.brekka.pegasus.core.model.Actor, org.brekka.pegasus.core.model.Division)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public <Owner extends Actor> Partnership<Owner, Organization> createOrganization(UUID idToAssign, String name,
            String tokenStr, String domainNameStr, OrganizationType details, Owner owner, Division<Owner> owningDivision) {
        Organization organization = createOrganization(name, tokenStr, domainNameStr, idToAssign);
        Partnership<Owner, Organization> partnership = divisionService.createDivisionPartnership(owningDivision, organization, null, null);
        Division<Organization> globalDivision = partnership.getTarget();
        organization.setGlobalDivision(globalDivision);
        applyDetailsXML(details, globalDivision);
        return partnership;
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
    public XmlEntity<OrganizationDocument> createOrganizationDetails(UUID orgId, OrganizationType organizationType) {
        Organization organization = organizationDAO.retrieveById(orgId);
        XmlEntity<OrganizationDocument> entity = applyDetailsXML(organizationType, organization.getGlobalDivision());
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
        releaseXml(releaseXml, organization);
        return organization;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.OrganizationService#retrieveByToken(java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Organization retrieveByToken(String tokenPath, boolean releaseXml) {
        Token token = tokenService.retrieveByPath(tokenPath);
        Organization organization = organizationDAO.retrieveByToken(token);
        releaseXml(releaseXml, organization);
        return organization;
    }
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Associate createAssociate(Organization organization, Member owner, EMailAddress eMailAddress) {
        // Add current user as an associate
        Associate associate = new Associate();
        associate.setOrganization(organization);
        associate.setStatus(ActorStatus.ACTIVE);
        associate.setMember(owner);
        associate.setPrimaryEMailAddress(eMailAddress);
        associateDAO.create(associate);
        return associate;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.OrganizationService#exists(java.util.UUID)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public boolean organizationExists(UUID orgId) {
        return organizationDAO.retrieveById(orgId) != null;
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
    public List<Associate> retrieveAssociates(Member member) {
        List<Associate> asociateList = associateDAO.retrieveAssociates(member);
        return asociateList;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.OrganizationService#retrieveEnlistment(org.brekka.pegasus.core.model.Division, org.brekka.pegasus.core.model.Member)
     */
    @Override
    public Enlistment retrieveEnlistment(Member member, Division<Organization> target) {
        Associate associate = retrieveAssociate(target.getOwner(), member);
        Enlistment enlistment = enlistmentDAO.retrieveEnlistmentByTarget(target, associate);
        return enlistment;
    }

    /**
     * @param details
     * @return
     */
    protected XmlEntity<OrganizationDocument> applyDetailsXML(OrganizationType organizationType, Division<Organization> globalDivision) {
        OrganizationDocument organizationDocument = OrganizationDocument.Factory.newInstance();
        if (organizationType == null) {
            organizationDocument.addNewOrganization();
        } else {
            organizationDocument.setOrganization(organizationType);
        }
        Organization organization = globalDivision.getOwner();
        XmlEntity<OrganizationDocument> entity = xmlEntityService.persistEncryptedEntity(organizationDocument, globalDivision);
        organization.setXml(entity);
        return entity;
    }
    

    /**
     * @param releaseXml
     * @param organization
     */
    protected void releaseXml(boolean releaseXml, Organization organization) {
        if (organization.getXml() != null && releaseXml) {
            XmlEntity<OrganizationDocument> entity = xmlEntityService.retrieveEntity(organization.getXml().getId(), OrganizationDocument.class);
            organization.setXml(entity);
        }
    }
    
    
    protected Organization createOrganization(String name, String tokenStr, String domainNameStr, UUID idToAssign) {
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
}
