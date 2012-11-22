/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import org.brekka.pegasus.core.model.AccessorContext;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Person;
import org.brekka.pegasus.core.security.PegasusAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
class AuthenticatedPersonImpl extends AuthenticatedMemberBase implements UserDetails {
    
    /**
     * Serial UID
     */
    private static final long serialVersionUID = 9046548671035895704L;
    
    /**
     * Will always be the person instance that corresponds to the login.
     */
    private Person person;
    
    private final Set<PegasusAuthority> authorities;
    
    private final AccessorContext context = new AccessorContext();
    
    public AuthenticatedPersonImpl(Person person, Set<PegasusAuthority> authorities) {
        this.person = person;
        setActiveActor(person);
        this.authorities = authorities;
    }
    
    public AuthenticatedPersonImpl(Person person, PegasusAuthority... authorities) {
        this(person, toSet(authorities));
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
        setActiveActor(person);
        this.authorities.remove(PegasusAuthority.MEMBER_SIGNUP);
        this.authorities.remove(PegasusAuthority.ANONYMOUS);
        this.authorities.add(PegasusAuthority.USER);
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
        return person.getAuthenticationToken().getUsername();
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
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.Accessor#getContext()
     */
    @Override
    public AccessorContext getContext() {
        return context;
    }
    

    /**
     * @param authorities2
     * @return
     */
    private static Set<PegasusAuthority> toSet(PegasusAuthority[] authoritiesArr) {
        Set<PegasusAuthority> authorities = EnumSet.noneOf(PegasusAuthority.class);
        for (PegasusAuthority pegasusAuthority : authoritiesArr) {
            authorities.add(pegasusAuthority);
        }
        return authorities;
    }

}
