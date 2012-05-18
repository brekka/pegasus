/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.brekka.pegasus.core.dao.MemberDAO;
import org.brekka.pegasus.core.model.ActorStatus;
import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.EMailAddress;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Person;
import org.brekka.pegasus.core.model.Profile;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.EMailAddressService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.ProfileService;
import org.brekka.pegasus.core.services.VaultService;
import org.brekka.phalanx.api.model.AuthenticatedPrincipal;
import org.brekka.phalanx.api.services.PhalanxService;
import org.brekka.stillingar.annotations.Configured;
import org.brekka.xml.pegasus.v1.config.PegasusDocument.Pegasus.Administration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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
    private VaultService vaultService; 
    
    @Autowired
    private ProfileService profileService;
    
    @Autowired
    private PhalanxService phalanxService;
    
    @Autowired
    private EMailAddressService eMailAddressService;
    
    @Configured
    private Administration administrationConfig;
    
    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public UserDetails loadUserByUsername(String openId) throws UsernameNotFoundException {
        Person person = (Person) memberDAO.retrieveByOpenId(openId);
        boolean admin = false;
        if (person == null) {
            // Not a member yet, create a new entry
            person = new Person();
            person.setOpenId(openId);
            memberDAO.create(person);
        } else {
            List<String> userOpenIDList = administrationConfig.getUserOpenIDList();
            for (String adminOpenId : userOpenIDList) {
                if (adminOpenId.equals(openId)) {
                    admin = true;
                    break;
                }
            }
        }
        AuthenticatedPersonImpl authMember = new AuthenticatedPersonImpl(person, admin);
        return authMember;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.MemberService#isNewMember()
     */
    @Override
    public boolean isNewMember() {
        Member member = getManaged();
        return member.getStatus() == ActorStatus.NEW;
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
    
    private AuthenticatedPersonImpl getAuthenticatedMember(SecurityContext securityContext) {
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
