/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.OpenVault;
import org.brekka.pegasus.core.model.Profile;
import org.brekka.pegasus.core.model.XmlEntity;
import org.brekka.xml.pegasus.v1.model.ProfileDocument;
import org.brekka.xml.pegasus.v1.model.ProfileType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author Andrew Taylor
 *
 */
class AuthenticatedMemberImpl extends User implements AuthenticatedMember, UserDetails {
    
    private static final List<GrantedAuthority> USER_AUTHORITIES = Arrays.<GrantedAuthority>
            asList(new SimpleGrantedAuthority("ROLE_USER"));
    
    /**
     * Serial UID
     */
    private static final long serialVersionUID = 9046548671035895704L;
    
    private Member member;
    
    private transient OpenVault activeVault;
    
    private transient Map<UUID, OpenVault> vaults;
    
    private transient Profile activeProfile;

    public AuthenticatedMemberImpl(Member member) {
        super(member.getOpenId(), "notused", USER_AUTHORITIES);
        this.member = member;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.AuthenticatedMember#getMember()
     */
    @Override
    public Member getMember() {
        return member;
    }

    /**
     * @param activeVault the activeVault to set
     */
    void setActiveVault(OpenVault activeVault) {
        this.activeVault = activeVault;
    }
    
    /**
     * @param activeProfile the activeProfile to set
     */
    void setActiveProfile(Profile activeProfile) {
        this.activeProfile = activeProfile;
    }
    
    /**
     * @return the activeProfile
     */
    Profile getActiveProfile() {
        return activeProfile;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.AuthenticatedMember#getProfile()
     */
    @Override
    public ProfileType getProfile() {
        if (activeProfile == null) {
            return null;
        }
        XmlEntity<ProfileDocument> xmlEntity = activeProfile.getXml();
        ProfileDocument bean = xmlEntity.getBean();
        if (bean == null) {
            return null;
        }
        return bean.getProfile();
    }
    
    /**
     * @param member the member to set
     */
    void setMember(Member member) {
        this.member = member;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.AuthenticatedMember#getActiveVault()
     */
    @Override
    public OpenVault getActiveVault() {
        return activeVault;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.AuthenticatedMember#getVault(java.util.UUID)
     */
    @Override
    public OpenVault getVault(UUID vaultId) {
        return vaults.get(vaultId);
    }

    /**
     * @param openVault
     */
    public void retainVault(OpenVault openVault) {
        UUID vaultId = openVault.getVault().getId();
        vaultMap().put(vaultId, openVault);
        if (member.getDefaultVault().getId().equals(vaultId)) {
            // This is the active vault
            this.activeVault = openVault;
        }
    }
    
    public OpenVault retrieveVault(UUID vaultId) {
        return vaultMap().get(vaultId);
    }
    
    private synchronized Map<UUID, OpenVault> vaultMap() {
        Map<UUID, OpenVault> map = this.vaults;
        if (map == null) {
            this.vaults = new HashMap<>();
        }
        return this.vaults;
    }
}
