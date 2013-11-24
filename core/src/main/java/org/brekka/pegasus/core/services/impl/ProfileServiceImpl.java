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

import java.util.List;

import org.brekka.commons.persistence.support.EntityUtils;
import org.brekka.pegasus.core.dao.ProfileDAO;
import org.brekka.pegasus.core.event.VaultOpenEvent;
import org.brekka.pegasus.core.event.XmlEntityDeleteEvent;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Person;
import org.brekka.pegasus.core.model.Profile;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.model.XmlEntity;
import org.brekka.pegasus.core.services.KeySafeService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.ProfileService;
import org.brekka.pegasus.core.services.XmlEntityService;
import org.brekka.xml.pegasus.v2.model.ProfileDocument;
import org.brekka.xml.pegasus.v2.model.ProfileType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Profile contains information about a user, preferences etc.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Service
@Transactional
public class ProfileServiceImpl implements ProfileService, ApplicationListener<ApplicationEvent> {

    @Autowired
    private ProfileDAO profileDAO;

    @Autowired
    private XmlEntityService xmlEntityService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private KeySafeService keySafeService;

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.ProfileService#createPlainProfile(org.brekka.pegasus.core.model.Member)
     */
    @Override
    @Transactional()
    public Profile createPlainProfile(final Member member, final ProfileType profileType) {
        Profile profile = new Profile();
        profile.setOwner(member);

        ProfileDocument profileDocument = prepareProfileDocument(profileType);

        XmlEntity<ProfileDocument> xmlEntity = this.xmlEntityService.persistPlainEntity(profileDocument, false);
        profile.setXml(xmlEntity);

        this.profileDAO.create(profile);
        return profile;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.ProfileService#createEncryptedProfile(org.brekka.pegasus.core.model.Member, org.brekka.pegasus.core.model.Vault)
     */
    @Override
    @Transactional()
    public Profile createEncryptedProfile(final Member member, final ProfileType profileType, final KeySafe<? extends Member> keySafe) {
        Profile profile = new Profile();
        profile.setOwner(member);

        ProfileDocument profileDocument = prepareProfileDocument(profileType);

        XmlEntity<ProfileDocument> xmlEntity = this.xmlEntityService.persistEncryptedEntity(profileDocument, keySafe, false);
        profile.setXml(xmlEntity);

        this.profileDAO.create(profile);
        return profile;
    }


    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.ProfileService#retrieveProfile(org.brekka.pegasus.core.model.Member)
     */
    @Override
    @Transactional(readOnly=true)
    public Profile retrieveProfile(final Member member) {
        Member nMember = EntityUtils.narrow(member, Member.class);
        List<Profile> profileList = this.profileDAO.retrieveByMember(nMember);
        if (profileList.size() == 0) {
            return null;
        }
        Profile profile = profileList.get(0);
        XmlEntity<ProfileDocument> xmlEntity = profile.getXml();
        if (xmlEntity.getCryptedDataId() == null) {
            // We can extract the model
            XmlEntity<ProfileDocument> managedXmlEntity = this.xmlEntityService.retrieveEntity(xmlEntity.getId(), ProfileDocument.class);
            ProfileDocument profileBean = managedXmlEntity.getBean();
            xmlEntity.setBean(profileBean);
            if (nMember instanceof Person) {
                ((Person) nMember).setFullName(profileBean.getProfile().getFullName());
            }

        }
        return profile;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.ProfileService#delete(org.brekka.pegasus.core.model.Profile)
     */
    @Override
    @Transactional()
    public void delete(final Profile profile) {
        this.profileDAO.delete(profile.getId());
    }

    @Override
    @Transactional()
    public void deleteFor(final Member member) {
        Member nMember = EntityUtils.narrow(member, Member.class);
        List<Profile> profileList = this.profileDAO.retrieveByMember(nMember);
        for (Profile profile : profileList) {
            this.profileDAO.delete(profile.getId());
        }
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.ProfileService#currentUserProfileUpdated()
     */
    @Override
    public void currentUserProfileUpdated() {
        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        for (TransactionSynchronization transactionSynchronization : synchronizations) {
            if (transactionSynchronization instanceof ProfileSynchronization) {
                // Already added for update
                return;
            }
        }
        AuthenticatedMemberBase<Member> current = AuthenticatedMemberBase.getCurrent(this.memberService, Member.class);
        Profile activeProfile = current.getActiveProfile();
        TransactionSynchronizationManager.registerSynchronization(new ProfileSynchronization(activeProfile));
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    @Override
    public void onApplicationEvent(final ApplicationEvent event) {
        if (event instanceof VaultOpenEvent) {
            VaultOpenEvent openEvent = (VaultOpenEvent) event;
            AuthenticatedMemberBase<Member> currentMember = AuthenticatedMemberBase.getCurrent(this.memberService, Member.class);

            // Check the profile, release it if necessary
            Profile activeProfile = currentMember.getActiveProfile();
            boolean released = releaseProfile(activeProfile, openEvent.getVault());
            Member nMember = EntityUtils.narrow(currentMember.getMember(), Member.class);
            if (released && nMember instanceof Person) {
                String fullName = activeProfile.getXml().getBean().getProfile().getFullName();
                ((Person) currentMember.getMember()).setFullName(fullName);
            }
        } else if (event instanceof XmlEntityDeleteEvent) {
            XmlEntityDeleteEvent deleteEvent = (XmlEntityDeleteEvent) event;
            Profile profile = this.profileDAO.retrieveByXmlEntity(deleteEvent.getXmlEntity());
            this.profileDAO.delete(profile.getId());
        }
    }

    private static ProfileDocument prepareProfileDocument(final ProfileType profileType) {
        ProfileDocument profileDocument = ProfileDocument.Factory.newInstance();
        profileDocument.setProfile(profileType);
        return profileDocument;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.ProfileService#releaseProfile(org.brekka.pegasus.core.model.Profile, org.brekka.pegasus.core.model.Vault)
     */
    protected boolean releaseProfile(final Profile profile, final Vault vault) {
        if (profile == null) {
            return false;
        }
        if (profile.getXml().getBean() != null) {
            // The bean is already unlocked
            return false;
        }
        // Need a session attached profile
        Profile managed = this.profileDAO.retrieveById(profile.getId());
        XmlEntity<ProfileDocument> xmlEntity = managed.getXml();
        // Apply back to the original profile
        profile.setXml(xmlEntity);
        KeySafe<?> nKeySafe = xmlEntity.getKeySafe();
        Vault protectedBy = null;
        while (nKeySafe != null) {
            nKeySafe = EntityUtils.narrow(nKeySafe, KeySafe.class);
            if (nKeySafe instanceof Division) {
                Division<?> division = (Division<?>) nKeySafe;
                nKeySafe = this.keySafeService.retrieveById(division.getParent().getId());
            } else if (nKeySafe instanceof Vault) {
                protectedBy = (Vault) nKeySafe;
                break;
            } else {
                throw new IllegalStateException(String.format("Unknown keySafe type '%s'", nKeySafe.getClass()));
            }
        }
        if (protectedBy == null
                || !EntityUtils.identityEquals(protectedBy, vault)) {
            // This is not the vault we are looking for.
            return false;
        }
        // Unlock it
        XmlEntity<ProfileDocument> managedXmlEntity = this.xmlEntityService.retrieveEntity(xmlEntity.getId(), ProfileDocument.class);
        xmlEntity.setBean(managedXmlEntity.getBean());
        return true;
    }

    private class ProfileSynchronization extends TransactionSynchronizationAdapter {

        private final Profile activeProfile;

        public ProfileSynchronization(final Profile activeProfile) {
            this.activeProfile = activeProfile;
        }

        /* (non-Javadoc)
         * @see org.springframework.transaction.support.TransactionSynchronizationAdapter#beforeCommit(boolean)
         */
        @Override
        public void beforeCommit(final boolean readOnly) {
            ProfileDocument profileDocument = this.activeProfile.getXml().getBean();
            XmlEntity<ProfileDocument> currentXml = this.activeProfile.getXml();
            XmlEntity<ProfileDocument> replacementXml;
            if (currentXml.getCryptedDataId() == null) {
                // Plain
                replacementXml = ProfileServiceImpl.this.xmlEntityService.persistPlainEntity(profileDocument, false);
            } else {
                KeySafe<?> keySafe = currentXml.getKeySafe();
                replacementXml = ProfileServiceImpl.this.xmlEntityService.persistEncryptedEntity(profileDocument, keySafe, false);
            }
            ProfileServiceImpl.this.xmlEntityService.delete(currentXml.getId());
            this.activeProfile.setXml(replacementXml);
            ProfileServiceImpl.this.profileDAO.update(this.activeProfile);
        }

        /* (non-Javadoc)
         * @see org.springframework.transaction.support.TransactionSynchronizationAdapter#getOrder()
         */
        @Override
        public int getOrder() {
            // Execute before the Hibernate session logic.
            return 100;
        }
    }

}
