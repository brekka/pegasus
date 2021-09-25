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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brekka.commons.persistence.support.EntityUtils;
import org.brekka.pegasus.core.dao.ProfileDAO;
import org.brekka.pegasus.core.event.VaultOpenEvent;
import org.brekka.pegasus.core.event.XmlEntityDeleteEvent;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.MemberContext;
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
import org.springframework.transaction.annotation.Propagation;
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
public class ProfileServiceImpl implements ProfileService, ApplicationListener<ApplicationEvent> {


    private static final Log log = LogFactory.getLog(ProfileServiceImpl.class);

    @Autowired
    private ProfileDAO profileDAO;

    @Autowired
    private XmlEntityService xmlEntityService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private KeySafeService keySafeService;

    @Override
    @Transactional()
    public Profile createPlainProfile(final Member member, final ProfileType profileType) {
        Profile profile = new Profile();
        profile.setOwner(member);

        ProfileDocument profileDocument = prepareProfileDocument(profileType);

        XmlEntity<ProfileDocument> xmlEntity = this.xmlEntityService.persistPlainEntity(profileDocument);
        profile.setXml(xmlEntity);

        this.profileDAO.create(profile);
        return profile;
    }

    @Override
    @Transactional()
    public Profile createEncryptedProfile(final Member member, final ProfileType profileType, final KeySafe<? extends Member> keySafe) {
        Profile profile = new Profile();
        profile.setOwner(member);

        ProfileDocument profileDocument = prepareProfileDocument(profileType);

        XmlEntity<ProfileDocument> xmlEntity = this.xmlEntityService.persistEncryptedEntity(profileDocument, keySafe);
        profile.setXml(xmlEntity);

        this.profileDAO.create(profile);
        return profile;
    }

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

    @Override
    @Transactional()
    public void delete(final Profile profile) {
        this.profileDAO.delete(profile.getId());
    }

    @Override
    @Transactional()
    public void update(final Profile profile) {
        this.profileDAO.update(profile);
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

    @Override
    @Transactional(propagation=Propagation.MANDATORY)
    public void currentUserProfileUpdated() {
        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        for (TransactionSynchronization transactionSynchronization : synchronizations) {
            if (transactionSynchronization instanceof ProfileSynchronization) {
                // Already added for update
                return;
            }
        }
        MemberContext current = memberService.retrieveCurrent();
        Profile activeProfile = current.getActiveProfile();
        if (activeProfile.getId() != null) {
            TransactionSynchronizationManager.registerSynchronization(new ProfileSynchronization(activeProfile));
        }
    }

    @Override
    public void onApplicationEvent(final ApplicationEvent event) {
        if (event instanceof VaultOpenEvent) {
            VaultOpenEvent openEvent = (VaultOpenEvent) event;
            MemberContext current = memberService.retrieveCurrent();

            // Check the profile, release it if necessary
            Profile activeProfile = current.getActiveProfile();
            boolean released = releaseProfile(activeProfile, openEvent.getVault());
            Member member = EntityUtils.narrow(current.getMember(), Member.class);
            if (released
                    && member instanceof Person) {
                XmlEntity<ProfileDocument> xmlEntity = activeProfile.getXml();
                ProfileDocument profileDocument = xmlEntity.getBean();
                ProfileType profile = profileDocument.getProfile();
                String fullName = profile.getFullName();
                Person person = (Person) member;
                person.setFullName(fullName);
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

    protected boolean releaseProfile(final Profile profile, final Vault vault) {
        if (profile == null
                || profile.getId() == null) {
            // There is no profile (could be placeholder)
            return false;
        }
        if (profile.getXml().getBean() != null) {
            // The bean is already unlocked
            return false;
        }
        // Need a session attached profile
        Profile managed = profileDAO.retrieveById(profile.getId());
        XmlEntity<ProfileDocument> xmlEntity = managed.getXml();
        KeySafe<?> nKeySafe = xmlEntity.getKeySafe();
        Vault protectedBy = null;
        while (nKeySafe != null) {
            nKeySafe = EntityUtils.narrow(nKeySafe, KeySafe.class);
            if (nKeySafe instanceof Division) {
                Division<?> division = (Division<?>) nKeySafe;
                nKeySafe = keySafeService.retrieveById(division.getParent().getId());
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
        XmlEntity<ProfileDocument> managedXmlEntity = xmlEntityService.retrieveEntity(xmlEntity.getId(), ProfileDocument.class);
        xmlEntity.setBean(managedXmlEntity.getBean());
        // Apply back to the original profile
        profile.setXml(xmlEntity);
        return true;
    }

    private class ProfileSynchronization extends TransactionSynchronizationAdapter {

        private final Profile activeProfile;

        public ProfileSynchronization(final Profile activeProfile) {
            this.activeProfile = activeProfile;
        }

        @Override
        public void beforeCommit(final boolean readOnly) {
            ProfileDocument profileDocument = activeProfile.getXml().getBean();
            XmlEntity<ProfileDocument> currentXml = activeProfile.getXml();
            XmlEntity<ProfileDocument> replacementXml;
            if (currentXml.getCryptedDataId() == null) {
                // Plain
                replacementXml = xmlEntityService.persistPlainEntity(profileDocument);
            } else {
                KeySafe<?> keySafe = currentXml.getKeySafe();
                replacementXml = xmlEntityService.persistEncryptedEntity(profileDocument, keySafe);
            }
            // Session managed instance
            Profile managed = profileDAO.retrieveById(activeProfile.getId());
            managed.setXml(replacementXml);
            if (log.isInfoEnabled()) {
                log.info(String.format("Updating profile '%s' XML entity '%s' (v %d)",
                        managed.getId(), replacementXml.getId(), replacementXml.getVersion()));
            }
            profileDAO.update(managed);
            // Update the reference bound to the member context
            activeProfile.setXml(replacementXml);
            xmlEntityService.delete(currentXml.getId());
        }

        @Override
        public int getOrder() {
            // Execute before the Hibernate session logic.
            return 100;
        }
    }
}
