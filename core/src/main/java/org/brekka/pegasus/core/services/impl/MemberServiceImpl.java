/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.brekka.pegasus.core.dao.MemberDAO;
import org.brekka.pegasus.core.dao.OpenIdDAO;
import org.brekka.pegasus.core.model.ActorStatus;
import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.EMailAddress;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.OpenID;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Person;
import org.brekka.pegasus.core.model.Profile;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.security.PegasusAuthority;
import org.brekka.pegasus.core.services.EMailAddressService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.OrganizationService;
import org.brekka.pegasus.core.services.ProfileService;
import org.brekka.pegasus.core.services.VaultService;
import org.brekka.phalanx.api.model.AuthenticatedPrincipal;
import org.brekka.phalanx.api.services.PhalanxService;
import org.brekka.stillingar.api.annotations.Configured;
import org.brekka.xml.pegasus.v1.config.PegasusDocument.Pegasus.Administration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Andrew Taylor
 *
 */
@Service
@Transactional
@Configured
public class MemberServiceImpl implements UserDetailsService, MemberService {

    @Autowired
    private MemberDAO memberDAO;
    
    @Autowired
    private OpenIdDAO openIdDAO;

    @Autowired
    private VaultService vaultService; 
    
    @Autowired
    private ProfileService profileService;
    
    @Autowired
    private PhalanxService phalanxService;
    
    @Autowired
    private EMailAddressService eMailAddressService;
    
    @Autowired
    private OrganizationService organizationService;
    
    @Configured
    private Administration administrationConfig;
    
    
    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public UserDetails loadUserByUsername(String openIdUri) throws UsernameNotFoundException {
        Set<PegasusAuthority> authorities = EnumSet.noneOf(PegasusAuthority.class);
        OpenID openId = openIdDAO.retrieveByURI(openIdUri);
        Person person = null;
        if (openId != null) {
            person = (Person) memberDAO.retrieveByAuthenticationToken(openId);
        }
        if (person != null 
                && person.getStatus() != ActorStatus.NEW) {
            List<String> userOpenIDList = administrationConfig.getUserOpenIDList();
            for (String adminOpenId : userOpenIDList) {
                if (adminOpenId.equals(openId)) {
                    authorities.add(PegasusAuthority.ADMIN);
                    break;
                }
            }
            authorities.add(PegasusAuthority.USER);
        } else {
            // Not a member yet, create a new entry
            if (person == null) {
                person = new Person();
                OpenID openID = new OpenID();
                openID.setUri(openIdUri);
                openIdDAO.create(openID);
                person.setAuthenticationToken(openID);
                memberDAO.create(person);
            }
            authorities.add(PegasusAuthority.MEMBER_SIGNUP);
            authorities.add(PegasusAuthority.ANONYMOUS);
        }
        AuthenticatedPersonImpl authMember = new AuthenticatedPersonImpl(person, authorities);
        return authMember;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.MemberService#activateOrganization(org.brekka.pegasus.core.model.Organization)
     */
    @Override
    public void activateOrganization(Organization organization) {
        AuthenticatedMemberBase current = (AuthenticatedMemberBase) getCurrent();
        Member member = current.getMember();
        Associate associate = organizationService.retrieveAssociate(organization, member);
        associate.setMember(member);
        current.setActiveActor(associate);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.MemberService#activateMember()
     */
    @Override
    public void activateMember() {
        AuthenticatedMemberBase current = (AuthenticatedMemberBase) getCurrent();
        Member member = current.getMember();
        current.setActiveActor(member);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.MemberService#isNewMember()
     */
    @Override
    public boolean isNewMember() {
        AuthenticatedMember authMem = getCurrent();
        return authMem != null 
             && authMem.getMember().getStatus() == ActorStatus.NEW;
    }
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void setupMember(String fullName, String email, String vaultPassword, boolean encryptedProfile) {
        Person managed = (Person) getManaged();
        managed.setFullName(fullName);
        managed.setStatus(ActorStatus.ACTIVE);
        
        // Binding to context
        AuthenticatedMember current = getCurrent();
        AuthenticatedPersonImpl authenticatedPersonImpl = (AuthenticatedPersonImpl) current;
        authenticatedPersonImpl.setPerson(managed);
        
        // Vault
        Vault defaultVault = vaultService.createVault("Default", vaultPassword, managed);
        managed.setDefaultVault(defaultVault);
        vaultService.openVault(defaultVault, vaultPassword);
        
        
        // Profile
        Profile profile;
        if (encryptedProfile) {
            profile = profileService.createEncryptedProfile(managed, defaultVault);
        } else {
            profile = profileService.createPlainProfile(managed);
        }
        authenticatedPersonImpl.setActiveProfile(profile);
        
        // E-Mail - needs to happen once profile/context are set
        if (StringUtils.isNotBlank(email)) {
            EMailAddress emailAddress = eMailAddressService.createEMail(email, managed, false);
            managed.setDefaultEmailAddress(emailAddress);
        }
        
        memberDAO.update(managed);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.MemberService#logout(org.springframework.security.core.context.SecurityContext)
     */
    @Override
    public void logout(SecurityContext securityContext) {
        AuthenticatedPersonImpl authenticatedMember = getAuthenticatedMember(securityContext);
        if (authenticatedMember != null) {
            List<AuthenticatedPrincipal> authenticatedPrincipals = authenticatedMember.clearVaults();
            for (AuthenticatedPrincipal authenticatedPrincipal : authenticatedPrincipals) {
                phalanxService.logout(authenticatedPrincipal);
            }
        }   
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.MemberService#hasAccess(org.springframework.security.core.GrantedAuthority)
     */
    @Override
    public boolean hasAccess(GrantedAuthority anonymousTransfer) {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.contains(anonymousTransfer);
    }
    
    protected Member getManaged() {
        AuthenticatedMember current = getCurrent();
        Member member = current.getMember();
        return memberDAO.retrieveById(member.getId());
    }
    
    @Override
    public AuthenticatedMember getCurrent() {
        SecurityContext context = SecurityContextHolder.getContext();
        AuthenticatedPersonImpl authMember = getAuthenticatedMember(context);
        if (authMember != null) {
            Member member = authMember.getMember();
            // Attempt to locate the user profile
            if (member.getStatus() == ActorStatus.ACTIVE
                    && authMember.getActiveProfile() == null) {
                Profile profile = profileService.retrieveProfile(member);
                authMember.setActiveProfile(profile);
            }
        }
        return authMember;
    }
    
    private static AuthenticatedPersonImpl getAuthenticatedMember(SecurityContext securityContext) {
        Authentication authentication = securityContext.getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedPersonImpl) {
            return (AuthenticatedPersonImpl) principal;
        }
        return null;
    }
}
