/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.Set;

import org.brekka.pegasus.core.model.Person;
import org.brekka.pegasus.core.security.PegasusAuthority;
import org.springframework.security.core.GrantedAuthority;

/**
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
class AuthenticatedPersonImpl extends AuthenticatedMemberBase<Person> {
    
    /**
     * Serial UID
     */
    private static final long serialVersionUID = 9046548671035895704L;
    
    public AuthenticatedPersonImpl(Person person, Set<GrantedAuthority> authorities) {
        super(person, authorities);
    }
    
    public AuthenticatedPersonImpl(Person person, GrantedAuthority... authorities) {
        this(person, toSet(authorities));
    }


    void setPerson(Person person) {
        setMember(person);
        setActiveActor(person);
        removeAuthority(PegasusAuthority.MEMBER_SIGNUP);
        removeAuthority(PegasusAuthority.ANONYMOUS);
        addAuthority(PegasusAuthority.USER);
    }
}
