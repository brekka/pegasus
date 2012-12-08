/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.List;

import org.brekka.pegasus.core.dao.ProfileDAO;
import org.brekka.pegasus.core.event.VaultOpenEvent;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Person;
import org.brekka.pegasus.core.model.Profile;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.model.XmlEntity;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.ProfileService;
import org.brekka.pegasus.core.services.XmlEntityService;
import org.brekka.xml.pegasus.v2.model.ProfileDocument;
import org.brekka.xml.pegasus.v2.model.ProfileType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Service
@Transactional
public class ProfileServiceImpl implements ProfileService, ApplicationListener<VaultOpenEvent> {

    @Autowired
    private ProfileDAO profileDAO;
    
    @Autowired
    private XmlEntityService xmlEntityService;
    
    @Autowired
    private MemberService memberService;
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.ProfileService#createPlainProfile(org.brekka.pegasus.core.model.Member)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Profile createPlainProfile(Member member) {
        Profile profile = new Profile();
        profile.setOwner(member);
        
        ProfileDocument profileDocument = createProfile(member);
        
        XmlEntity<ProfileDocument> xmlEntity = xmlEntityService.persistPlainEntity(profileDocument);
        profile.setXml(xmlEntity);
        
        profileDAO.create(profile);
        return profile;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.ProfileService#createEncryptedProfile(org.brekka.pegasus.core.model.Member, org.brekka.pegasus.core.model.Vault)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Profile createEncryptedProfile(Member member, Vault vault) {
        Profile profile = new Profile();
        profile.setOwner(member);
        
        ProfileDocument profileDocument = createProfile(member);
        
        XmlEntity<ProfileDocument> xmlEntity = xmlEntityService.persistEncryptedEntity(profileDocument, vault);
        profile.setXml(xmlEntity);
        
        profileDAO.create(profile);
        return profile;
    }
    
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.ProfileService#retrieveProfile(org.brekka.pegasus.core.model.Member)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Profile retrieveProfile(Member member) {
        List<Profile> profileList = profileDAO.retrieveByMember(member);
        if (profileList.size() == 0) {
            return null;
        }
        Profile profile = profileList.get(0);
        XmlEntity<ProfileDocument> xmlEntity = profile.getXml();
        if (xmlEntity.getCryptedDataId() == null) {
            // We can extract the model
            XmlEntity<ProfileDocument> managedXmlEntity = xmlEntityService.retrieveEntity(xmlEntity.getId(), ProfileDocument.class);
            ProfileDocument profileBean = managedXmlEntity.getBean();
            xmlEntity.setBean(profileBean);
            if (member instanceof Person) {
                ((Person) member).setFullName(profileBean.getProfile().getFullName());
            }
            
        }
        return profile;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.ProfileService#releaseProfile(org.brekka.pegasus.core.model.Profile, org.brekka.pegasus.core.model.Vault)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public boolean releaseProfile(Profile profile, Vault vault) {
        if (profile == null) {
            return false;
        }
        if (profile.getXml().getBean() != null) {
            // The bean is already unlocked
            return false;
        }
        XmlEntity<ProfileDocument> xmlEntity = profile.getXml();
        if (!xmlEntity.getKeySafe().getId().equals(vault.getId())) {
            // This is not the vault we are looking for.
            return false;
        }
        // Unlock it
        XmlEntity<ProfileDocument> managedXmlEntity = xmlEntityService.retrieveEntity(xmlEntity.getId(), ProfileDocument.class);
        xmlEntity.setBean(managedXmlEntity.getBean());
        return true;
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
        AuthenticatedMemberBase<Member> current = AuthenticatedMemberBase.getCurrent(memberService, Member.class);
        Profile activeProfile = current.getActiveProfile();
        TransactionSynchronizationManager.registerSynchronization(new ProfileSynchronization(activeProfile));
    }
    
    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    @Override
    public void onApplicationEvent(VaultOpenEvent event) {
        AuthenticatedMemberBase<Member> currentMember = AuthenticatedMemberBase.getCurrent(memberService, Member.class);

        // Check the profile, release it if necessary
        Profile activeProfile = currentMember.getActiveProfile();
        boolean released = releaseProfile(activeProfile, event.getVault());
        if (released 
                && currentMember.getMember() instanceof Person) {
            String fullName = activeProfile.getXml().getBean().getProfile().getFullName();
            ((Person) currentMember.getMember()).setFullName(fullName);
        }
    }

    private static ProfileDocument createProfile(Member member) {
        ProfileDocument profileDocument = ProfileDocument.Factory.newInstance();
        ProfileType profileType = profileDocument.addNewProfile();

        if (member instanceof Person) {
            profileType.setFullName(((Person) member).getFullName());
        }
        return profileDocument;
    }
    
    private class ProfileSynchronization extends TransactionSynchronizationAdapter {
        
        private Profile activeProfile;
        
        public ProfileSynchronization(Profile activeProfile) {
            this.activeProfile = activeProfile;
        }

        /* (non-Javadoc)
         * @see org.springframework.transaction.support.TransactionSynchronizationAdapter#beforeCommit(boolean)
         */
        @Override
        public void beforeCommit(boolean readOnly) {
            ProfileDocument profileDocument = activeProfile.getXml().getBean();
            XmlEntity<ProfileDocument> currentXml = activeProfile.getXml();
            XmlEntity<ProfileDocument> replacementXml;
            if (currentXml.getCryptedDataId() == null) {
                // Plain
                replacementXml = xmlEntityService.persistPlainEntity(profileDocument);
            } else {
                KeySafe keySafe = currentXml.getKeySafe();
                replacementXml = xmlEntityService.persistEncryptedEntity(profileDocument, keySafe);
            }
            xmlEntityService.delete(currentXml.getId());
            activeProfile.setXml(replacementXml);
            profileDAO.update(activeProfile);
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
