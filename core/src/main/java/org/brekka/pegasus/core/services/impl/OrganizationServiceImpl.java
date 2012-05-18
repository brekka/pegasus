/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.brekka.pegasus.core.dao.AssociateDAO;
import org.brekka.pegasus.core.dao.DivisionAssociateDAO;
import org.brekka.pegasus.core.dao.DivisionDAO;
import org.brekka.pegasus.core.dao.OrganizationDAO;
import org.brekka.pegasus.core.model.ActorStatus;
import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.DivisionAssociate;
import org.brekka.pegasus.core.model.DomainName;
import org.brekka.pegasus.core.model.EMailAddress;
import org.brekka.pegasus.core.model.KeySafeStatus;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Token;
import org.brekka.pegasus.core.model.TokenType;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.EMailAddressService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.OrganizationService;
import org.brekka.pegasus.core.services.TokenService;
import org.brekka.pegasus.core.services.VaultService;
import org.brekka.phalanx.api.model.KeyPair;
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
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.OrganizationService#createOrganization(java.lang.String, java.lang.String, java.lang.String, org.brekka.pegasus.core.model.Vault)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Organization createOrganization(String name, String tokenStr, String domainNameStr, 
            String ownerEmailStr, Member owner) {
        Organization organization = new Organization();
        organization.setName(name);
        
        if (StringUtils.isNotBlank(domainNameStr)) {
            DomainName domainName = eMailAddressService.toDomainName(domainNameStr);
            organization.setPrimaryDomainName(domainName);
        }
        Token token = tokenService.createToken(tokenStr, TokenType.ORG);
        organization.setToken(token);
        
        organizationDAO.create(organization);
        
        Vault vault = owner.getDefaultVault();
        
        // Create a new key pair
        KeyPair organizationKeyPair = vaultService.createKeyPair(vault);
        
        // Add current user as an associate
        Associate associate = new Associate();
        associate.setOrganization(organization);
        associate.setStatus(ActorStatus.ACTIVE);
        associate.setMember(owner);
        if (StringUtils.isNotBlank(ownerEmailStr)) {
            EMailAddress ownerEMail = eMailAddressService.createEMail(ownerEmailStr, owner, false);
            associate.setPrimaryEMailAddress(ownerEMail);
        }
        associate.setDefaultVault(vault);
        associate.setKeyPairId(organizationKeyPair.getId());
        associateDAO.create(associate);
        
        // Also create a default division
        KeyPair divisionKeyPair = vaultService.createKeyPair(vault);
        Division division = new Division();
        division.setOrganization(organization);
        division.setKeyPairId(divisionKeyPair.getId()); // Root division - used for the public key.
        division.setName("Company Home");
        division.setSlug("company_home");
        division.setStatus(KeySafeStatus.ACTIVE);
        divisionDAO.create(division);
        
        DivisionAssociate divisionAssociate = new DivisionAssociate();
        divisionAssociate.setAssociate(associate);
        divisionAssociate.setDivision(division);
        divisionAssociate.setKeyPairId(divisionKeyPair.getId());
        divisionAssociateDAO.create(divisionAssociate);
        return organization;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.OrganizationService#retrieveDivision(java.lang.String, java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Division retrieveDivision(String orgToken, String divisionSlug) {
        Organization organization = retrieveByToken(orgToken);
        Division division = divisionDAO.retrieveBySlug(organization, divisionSlug);
        return division;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.OrganizationService#retrieveDivisions(org.brekka.pegasus.core.model.Organization)
     */
    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public List<DivisionAssociate> retrieveCurrentDivisions() {
        AuthenticatedMemberBase currentMember = AuthenticatedMemberBase.getCurrent(memberService);
        Associate associate = (Associate) currentMember.getActiveActor();
        return divisionAssociateDAO.retrieveForOrg(associate);
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
