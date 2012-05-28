/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Person;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
class AuthenticatedPersonImpl extends AuthenticatedMemberBase implements UserDetails {
    
    public static final GrantedAuthority USER = new SimpleGrantedAuthority("ROLE_USER");
    public static final GrantedAuthority ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");
    
    private static final List<GrantedAuthority> USER_AUTHORITIES = Arrays.<GrantedAuthority>
            asList(USER);
    private static final List<GrantedAuthority> ADMIN_AUTHORITIES = Arrays.<GrantedAuthority>
            asList(USER, ADMIN);
    
    /**
     * Serial UID
     */
    private static final long serialVersionUID = 9046548671035895704L;
    
    /**
     * Will always be the person instance that corresponds to the login.
     */
    private Person person;
    
    private final List<GrantedAuthority> authorities;
    
    public AuthenticatedPersonImpl(Person person, boolean admin) {
        this.person = person;
        setActiveActor(person);
        this.authorities = admin ? ADMIN_AUTHORITIES : USER_AUTHORITIES;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.AuthenticatedMember#getMember()
     */
    @Override
    public Member getMember() {
        return person;
    }
    
    void setPerson(Person person) {
        this.person = person;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#getAuthorities()
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.AuthenticatedMember#hasAccess(org.springframework.security.core.GrantedAuthority)
     */
    @Override
    public boolean hasAccess(GrantedAuthority grantedAuthority) {
        return getAuthorities().contains(grantedAuthority);
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#getPassword()
     */
    @Override
    public String getPassword() {
        return "notused";
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#getUsername()
     */
    @Override
    public String getUsername() {
        return person.getOpenId();
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#isAccountNonExpired()
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#isAccountNonLocked()
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#isCredentialsNonExpired()
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

}
