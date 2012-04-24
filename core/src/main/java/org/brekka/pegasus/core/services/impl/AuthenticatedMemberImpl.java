/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.Arrays;
import java.util.List;

import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.OpenVault;
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
    
    private OpenVault activeVault;
    
    

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
}
