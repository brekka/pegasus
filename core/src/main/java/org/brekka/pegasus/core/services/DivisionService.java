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

package org.brekka.pegasus.core.services;

import java.util.List;

import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.Connection;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.Enlistment;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Partnership;
import org.brekka.phalanx.api.model.KeyPair;

/**
 * TODO Description of DivisionService
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface DivisionService {
    Enlistment createDivisionEnlistment(Associate associate, KeySafe<Member> protectedBy, String slug, String name);
    
    <Owner extends Actor, Target extends Actor> Partnership<Owner, Target> 
            createDivisionPartnership(Division<Owner> source, Target target, String slug, String name);

    <T extends Actor> Division<T> createDivision(Division<T> parent, String slug, String name);
    
    
    Enlistment createEnlistment(Associate toAssign, KeySafe<Member> assignToKeySafe, 
            Connection<Associate, KeySafe<Member>, Division<Organization>> existingEnlistment);
    
    /**
     * @param orgToken
     * @param divisionToken
     * @return
     */
    <T extends Actor> Division<T> retrieveDivision(T organization, String divisionSlug);
    
    /**
     * @param organization
     * @return
     */
    List<Enlistment> retrieveCurrentEnlistments();
}
