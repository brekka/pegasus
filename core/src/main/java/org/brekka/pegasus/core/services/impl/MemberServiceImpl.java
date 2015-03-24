/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.brekka.pegasus.core.services.impl;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.support.EntityUtils;
import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.dao.ActorDAO;
import org.brekka.pegasus.core.dao.MemberDAO;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.ActorStatus;
import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.AuthenticationToken;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.EMailAddress;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Person;
import org.brekka.pegasus.core.model.Profile;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.DivisionService;
import org.brekka.pegasus.core.services.EMailAddressService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.OrganizationService;
import org.brekka.pegasus.core.services.ProfileService;
import org.brekka.pegasus.core.services.VaultService;
import org.brekka.phalanx.api.model.AuthenticatedPrincipal;
import org.brekka.phalanx.api.services.PhalanxService;
import org.brekka.xml.pegasus.v2.model.EMailType;
import org.brekka.xml.pegasus.v2.model.ProfileType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handle membership
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Service
@Transactional
public class MemberServiceImpl implements MemberService {

    @Autowired
    private ActorDAO actorDAO;

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

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private DivisionService divisionService;


    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.MemberService#activateOrganization(org.brekka.pegasus.core.model.Organization)
     */
    @Override
    @Transactional()
    public void activateOrganization(final Organization organization) {
        AuthenticatedMemberBase<Member> current = (AuthenticatedMemberBase<Member>) getCurrent(Member.class);
        Member member = current.getMember();
        Associate associate = this.organizationService.retrieveAssociate(organization, member);
        if (associate == null) {
            throw new PegasusException(PegasusErrorCode.PG904,
                    "Current member '%s' is not an associate of organization '%s'", member.getId(), organization.getId());
        }
        Organization managedOrganization = this.organizationService.retrieveById(associate.getOrganization().getId(), true);
        associate.setOrganization(managedOrganization);
        associate.setMember(member);
        current.setActiveActor(associate);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.MemberService#retrieveById(java.util.UUID, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly=true)
    public <T extends Member> T retrieveById(final UUID memberId, final Class<T> expectedType) {
        Member member = this.memberDAO.retrieveById(memberId);
        if (!expectedType.isAssignableFrom(member.getClass())) {
            throw new PegasusException(PegasusErrorCode.PG903,
                    "Member is '%s' not the expected '%s'", member.getClass().getName(), expectedType.getName());
        }
        return (T) member;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.MemberService#retrievePerson(org.brekka.pegasus.core.model.AuthenticationToken)
     */
    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly=true)
    public <T extends Member> T retrieveMember(final AuthenticationToken token, final Class<T> expectedType) {
        Member member = this.memberDAO.retrieveByAuthenticationToken(token);
        if (member == null) {
            return null;
        }
        if (!expectedType.isAssignableFrom(member.getClass())) {
            throw new PegasusException(PegasusErrorCode.PG905,
                    "Member is '%s' not the expected '%s'", member.getClass().getName(), expectedType.getName());
        }
        return (T) member;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.MemberService#activateMember()
     */
    @Override
    public void activateMember() {
        AuthenticatedMemberBase<Member> current = (AuthenticatedMemberBase<Member>) getCurrent(Member.class);
        Member member = current.getMember();
        current.setActiveActor(member);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.MemberService#isNewMember()
     */
    @Override
    public boolean isNewMember() {
        AuthenticatedMember<Member> authMem = getCurrent(Member.class);
        return authMem != null
                && authMem.getMember().getStatus() == ActorStatus.NEW;
    }

    @Override
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void setupPerson(final ProfileType profileType, final String vaultPassword, final boolean encryptedProfile) {
        Person managed = getManaged(Person.class);
        populatePerson(managed, profileType, vaultPassword, encryptedProfile, false, true);
        this.memberDAO.update(managed);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.MemberService#createPerson(org.brekka.pegasus.core.model.AuthenticationToken, java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    @Override
    @Transactional()
    public Person createPerson(final AuthenticationToken authenticationToken, final ProfileType profileType,
            final String vaultPassword, final boolean encryptProfile) {
        Person person = new Person();
        person.setAuthenticationToken(authenticationToken);
        populatePerson(person, profileType, vaultPassword, encryptProfile, true, false);
        return person;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.MemberService#preparePerson(org.brekka.pegasus.core.model.OpenID)
     */
    @Override
    @Transactional()
    public Person createPerson(final AuthenticationToken authenticationToken) {
        Person person = new Person();
        person.setAuthenticationToken(authenticationToken);
        this.memberDAO.create(person);
        return person;
    }


    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.MemberService#logout(org.springframework.security.core.context.SecurityContext)
     */
    @Override
    @Transactional()
    public void logout(final SecurityContext securityContext) {
        AuthenticatedMemberBase<Member> authenticatedMember = getAuthenticatedMember(securityContext, Member.class);
        if (authenticatedMember != null) {
            List<AuthenticatedPrincipal> authenticatedPrincipals = authenticatedMember.clearVaults();
            for (AuthenticatedPrincipal authenticatedPrincipal : authenticatedPrincipals) {
                this.phalanxService.logout(authenticatedPrincipal);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.MemberService#hasAccess(org.springframework.security.core.GrantedAuthority)
     */
    @Override
    public boolean hasAccess(final GrantedAuthority authority) {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.contains(authority);
    }


    @Override
    @Transactional(readOnly=true)
    public <T extends Member> AuthenticatedMember<T> getCurrent(final Class<T> expectedType) {
        SecurityContext context = SecurityContextHolder.getContext();
        AuthenticatedMemberBase<T> authMember = getAuthenticatedMember(context, expectedType);
        if (authMember != null) {
            Member member = authMember.getMember();
            // Attempt to locate the user profile
            if (member.getStatus() == ActorStatus.ACTIVE
                    && authMember.getActiveProfile() == null) {
                Profile profile = this.profileService.retrieveProfile(member);
                authMember.setActiveProfile(profile);
            }
        }
        return authMember;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.MemberService#resetMember(org.brekka.pegasus.core.model.Member)
     */
    @Override
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void resetMember(final Member member) {
        Member managedMember = this.memberDAO.retrieveById(member.getId());

        // Clear all profiles for the member
        this.profileService.deleteFor(managedMember);

        // Delete the vault
        Vault vault = managedMember.getDefaultVault();
        if (vault != null) {
            this.vaultService.deleteVault(vault);
            managedMember.setDefaultVault(null);
        }

        // Update
        this.memberDAO.update(managedMember);

        // Update the context user, if appropriate.
        AuthenticatedMemberBase<Member> current = (AuthenticatedMemberBase<Member>) getCurrent(Member.class);
        if (EntityUtils.identityEquals(current.getMember(), managedMember)) {
            current.setMember(managedMember);
            current.setActiveActor(null);
            current.setActiveProfile(null);
        }
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.MemberService#getCurrent()
     */
    @Override
    public AuthenticatedMember<Member> getCurrent() {
        return getCurrent(Member.class);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.MemberService#updateStatus(java.util.UUID, org.brekka.pegasus.core.model.ActorStatus)
     */
    @Override
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void updateStatus(final UUID actorId, final ActorStatus status) {
        Actor managed = this.actorDAO.retrieveById(actorId);
        managed.setStatus(status);
        this.actorDAO.update(managed);
    }

    protected <M extends Member> M getManaged(final Class<M> memberType) {
        AuthenticatedMember<Member> current = getCurrent(Member.class);
        Member member = current.getMember();
        Member managed = this.memberDAO.retrieveById(member.getId());
        return EntityUtils.narrow(managed, memberType);
    }

    protected void populatePerson(final Person person, final ProfileType profileType, final String vaultPassword,
            final boolean encryptProfile, final boolean create, final boolean currentUser) {
        person.setFullName(profileType.getFullName());
        person.setStatus(ActorStatus.ACTIVE);
        if (create) {
            // Need to save now, person is reference
            this.memberDAO.create(person);
        }


        // Vault
        Vault defaultVault = this.vaultService.createVault("Default", vaultPassword, person);
        person.setDefaultVault(defaultVault);
        defaultVault = this.vaultService.openVault(defaultVault.getId(), vaultPassword);
        Division<Member> primaryDivision = this.divisionService.createDivision(defaultVault, null, "Primary");
        person.setPrimaryKeySafe(primaryDivision);

        List<EMailType> eMailList = profileType.getEMailList();
        for (EMailType eMailType : eMailList) {
            String address = eMailType.getAddress().toLowerCase();
            EMailAddress emailAddress = this.eMailAddressService.retrieveByAddress(address);
            if (emailAddress == null) {
                emailAddress = this.eMailAddressService.createEMail(address, person, false);
            }
            person.setDefaultEmailAddress(emailAddress);
            eMailType.setUUID(emailAddress.getId().toString());
            eMailType.setAddress(address); // Now confirmed lowercase.
        }


        // Profile
        Profile profile;
        if (encryptProfile) {
            profile = this.profileService.createEncryptedProfile(person, profileType, primaryDivision);
        } else {
            profile = this.profileService.createPlainProfile(person, profileType);
        }

        if (currentUser) {
            // Binding to context
            AuthenticatedMember<Person> current = getCurrent(Person.class);
            AuthenticatedMemberBase<Person> authenticatedPersonImpl = (AuthenticatedMemberBase<Person>) current;
            authenticatedPersonImpl.setMember(person);
            authenticatedPersonImpl.setActiveProfile(profile);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Member> AuthenticatedMemberBase<T> getAuthenticatedMember(final SecurityContext securityContext, final Class<T> expectedType) {
        Authentication authentication = securityContext.getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedMemberBase) {
            AuthenticatedMemberBase<?> base = (AuthenticatedMemberBase<?>) principal;
            Member member = base.getMember();
            if (!expectedType.isAssignableFrom(member.getClass())) {
                throw new PegasusException(PegasusErrorCode.PG902,
                        "Member is '%s' not the expected '%s'", member.getClass().getName(), expectedType.getName());
            }
            return (AuthenticatedMemberBase<T>) principal;
        }
        return null;
    }
}
