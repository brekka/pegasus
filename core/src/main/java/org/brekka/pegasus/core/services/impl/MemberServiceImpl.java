/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import org.brekka.pegasus.core.dao.MemberDAO;
import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.MemberStatus;
import org.brekka.pegasus.core.model.OpenVault;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.VaultService;
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
public class MemberServiceImpl implements UserDetailsService, MemberService {

    @Autowired
    private MemberDAO memberDAO;

    
    @Autowired
    private VaultService vaultService; 
    
    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public UserDetails loadUserByUsername(String openId) throws UsernameNotFoundException {
        Member member = memberDAO.retrieveByOpenId(openId);
        if (member == null) {
            // Not a member yet, create a new entry
            member = new Member();
            member.setOpenId(openId);
            member.setStatus(MemberStatus.NEW);
            memberDAO.create(member);
        }
        return new AuthenticatedMemberImpl(member);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.MemberService#isNewMember()
     */
    @Override
    public boolean isNewMember() {
        Member member = getManaged();
        return member.getStatus() == MemberStatus.NEW;
    }
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void setupMember(String name, String email, String vaultPassword) {
        Member managed = getManaged();
        managed.setName(name);
        managed.setEmail(email);
        managed.setStatus(MemberStatus.ACTIVE);
        
        Vault defaultVault = vaultService.createVault("Default", vaultPassword, managed);
        managed.setDefaultVault(defaultVault);
        
        memberDAO.update(managed);
        
        AuthenticatedMember current = getCurrent();
        
        AuthenticatedMemberImpl authenticatedMemberImpl = (AuthenticatedMemberImpl) current;
        
        OpenVault openVault = (OpenVault) vaultService.openVault(defaultVault, vaultPassword);
        authenticatedMemberImpl.setActiveVault(openVault);
        authenticatedMemberImpl.setMember(managed);
    }
    
    protected Member getManaged() {
        AuthenticatedMember current = getCurrent();
        Member member = current.getMember();
        return memberDAO.retrieveById(member.getId());
    }
    
    @Override
    public AuthenticatedMember getCurrent() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        return (AuthenticatedMember) authentication.getPrincipal();
    }
}
