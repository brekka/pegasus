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
import java.util.UUID;

import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.Connection;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.Enlistment;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Partnership;

/**
 * Division Service
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface DivisionService {
    Enlistment createDivisionEnlistment(Associate associate, KeySafe<? extends Member> protectedBy, String slug, String name);
    
    <Owner extends Actor, Target extends Actor> Partnership<Owner, Target> 
            createDivisionPartnership(Division<Owner> source, Target target, String slug, String name);
    
    <Owner extends Actor, Target extends Actor> Partnership<Owner, Target> createPartnership(Owner owner,
            Division<Owner> source, Division<Target> target);

    <T extends Actor> Division<T> createDivision(KeySafe<T> parent, String slug, String name);
    
    
    <Owner extends Actor, Source extends KeySafe<?>> Enlistment createEnlistment(Associate toAssign, KeySafe<? extends Member> assignToKeySafe, 
            Connection<Owner, Source, Division<Organization>> existingEnlistment);
    
    /**
     * @param orgToken
     * @param divisionToken
     * @return
     */
    <T extends Actor> Division<T> retrieveDivision(T organization, String divisionSlug);
    
    <Owner extends Actor, Target extends Actor> List<Partnership<Owner, Target>> retrievePartnershipsByTarget(Division<Target> target);
    
    /**
     * @param organization
     * @return
     */
    List<Enlistment> retrieveCurrentEnlistments();
    
    /**
     * Replace the keyPair in the division with that backed-up to a partnership identified by the division as the target.
     * 
     * @param division
     *            the division being restored
     * @param protectWith
     *            the keySafe to assign access to the keyPair that must have been previously backed up via a
     *            partnership.
     */
    <T extends Actor> void restoreDivision(Division<T> division, KeySafe<?> protectWith);
    
    /**
     * @param partnershipId
     * @return
     */
    <Owner extends Actor, Target extends Actor> Partnership<Owner, Target> retrievePartnershipById(UUID partnershipId);

}
