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
import org.brekka.pegasus.core.model.AuthenticationToken;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.EMailAddress;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.MemberContext;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Person;
import org.brekka.pegasus.core.model.Profile;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.security.PegasusPrincipal;
import org.brekka.pegasus.core.services.DivisionService;
import org.brekka.pegasus.core.services.EMailAddressService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.OrganizationService;
import org.brekka.pegasus.core.services.PegasusPrincipalService;
import org.brekka.pegasus.core.services.ProfileService;
import org.brekka.pegasus.core.services.VaultService;
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
    private EMailAddressService eMailAddressService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private DivisionService divisionService;

    @Autowired
    private PegasusPrincipalService pegasusPrincipalService;



    @Override
    @Transactional()
    public void activateOrganization(final Organization organization) {
        MemberContext memberContext = retrieveCurrent();
        Member member = memberContext.getMember();
        Associate associate = this.organizationService.retrieveAssociate(organization, member);
        if (associate == null) {
            throw new PegasusException(PegasusErrorCode.PG904,
                    "Current member '%s' is not an associate of organization '%s'", member.getId(), organization.getId());
        }
        Organization managedOrganization = this.organizationService.retrieveById(associate.getOrganization().getId(), true);
        associate.setOrganization(managedOrganization);
        associate.setMember(member);
        memberContext.setActiveActor(associate);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly=true)
    public <T extends Member> T retrieveById(final UUID memberId, final Class<T> expectedType) {
        Member member = this.memberDAO.retrieveById(memberId);
        if (!expectedType.isAssignableFrom(member.getClass())) {
            throw new PegasusException(PegasusErrorCode.PG903,
                    "Member is '%s', not the expected '%s'", member.getClass().getName(), expectedType.getName());
        }
        return (T) member;
    }

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

    @Override
    public void activateMember() {
        MemberContext current = currentContext();
        Member member = current.getMember();
        current.setActiveActor(member);
    }

    @Override
    public boolean isNewMember() {
        MemberContext current = currentContext();
        return current != null
            && current.getMember().getStatus() == ActorStatus.NEW;
    }

    @Override
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void setupPerson(final Person person, final ProfileType profileType, final String vaultPassword, final boolean encryptedProfile) {
        Person managed = EntityUtils.narrow(memberDAO.retrieveById(person.getId()), Person.class);
        populatePerson(managed, profileType, vaultPassword, encryptedProfile, false);
        this.memberDAO.update(managed);
    }

    @Override
    @Transactional()
    public Person createPerson(final AuthenticationToken authenticationToken, final ProfileType profileType,
            final String vaultPassword, final boolean encryptProfile) {
        Person person = new Person();
        person.setAuthenticationToken(authenticationToken);
        populatePerson(person, profileType, vaultPassword, encryptProfile, true);
        return person;
    }

    @Override
    @Transactional()
    public Person createPerson(final AuthenticationToken authenticationToken) {
        Person person = new Person();
        person.setAuthenticationToken(authenticationToken);
        this.memberDAO.create(person);
        return person;
    }



    @Override
    public boolean hasAccess(final GrantedAuthority authority) {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.contains(authority);
    }

    @Override
    public MemberContext getCurrent() {
        return currentContext();
    }

    @Override
    public MemberContext retrieveCurrent() {
        return currentContext(true);
    }

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
    }

    @Override
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void updateStatus(final UUID actorId, final ActorStatus status) {
        Actor managed = this.actorDAO.retrieveById(actorId);
        managed.setStatus(status);
        this.actorDAO.update(managed);
    }


    private void populatePerson(final Person person, final ProfileType profileType, final String vaultPassword,
            final boolean encryptProfile, final boolean create) {
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
        if (encryptProfile) {
            profileService.createEncryptedProfile(person, profileType, primaryDivision);
        } else {
            profileService.createPlainProfile(person, profileType);
        }
    }

    private MemberContextImpl currentContext() {
        return currentContext(false);
    }

    private MemberContextImpl currentContext(final boolean required) {
        MemberContextImpl memberContext = null;
        PegasusPrincipal currentPrincipal = pegasusPrincipalService.currentPrincipal(required);
        if (currentPrincipal != null) {
            memberContext = (MemberContextImpl) currentPrincipal.getMemberContext();
            if (memberContext != null) {
                Member member = memberContext.getMember();
                // Attempt to locate the user profile
                if (member.getStatus() == ActorStatus.ACTIVE
                        && memberContext.getActiveProfile() == null) {
                    Profile profile = profileService.retrieveProfile(member);
                    memberContext.setActiveProfile(profile);
                }
            }
        }
        return memberContext;
    }
}
