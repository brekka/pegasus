/*
 * Copyright 2013 the original author or authors.
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

import java.util.Set;

import org.brekka.pegasus.core.model.Person;
import org.brekka.pegasus.core.security.PegasusAuthority;
import org.springframework.security.core.GrantedAuthority;

/**
 * Identifies a specific person. Only accessible to Pegasus services.
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
