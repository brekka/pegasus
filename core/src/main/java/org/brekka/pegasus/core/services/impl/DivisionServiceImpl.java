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

package org.brekka.pegasus.core.services.impl;

import java.util.List;
import java.util.UUID;

import org.brekka.pegasus.core.dao.DivisionDAO;
import org.brekka.pegasus.core.dao.EnlistmentDAO;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.Connection;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.Enlistment;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.KeySafeStatus;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Partnership;
import org.brekka.pegasus.core.services.DivisionService;
import org.brekka.pegasus.core.services.KeySafeService;
import org.brekka.pegasus.core.services.VaultService;
import org.brekka.phalanx.api.beans.IdentityKeyPair;
import org.brekka.phalanx.api.model.KeyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * TODO Description of DivisionServiceImpl
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Service
@Transactional
public class DivisionServiceImpl extends AbstractKeySafeServiceSupport implements DivisionService {

    
    @Autowired
    private DivisionDAO divisionDAO;
    
    @Autowired
    private KeySafeService keySafeService;
    
    @Autowired
    private VaultService vaultService;
    
    @Autowired
    protected EnlistmentDAO  enlistmentDAO;
    
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.OrganizationService#createDivision(org.brekka.pegasus.core.model.Organization, java.lang.String, java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Enlistment createDivisionEnlistment(Associate associate, KeySafe<Member> protectedBy, String slug, String name) {
        KeyPair memberDivisionKeyPair = keySafeService.createKeyPair(protectedBy);
        KeyPair publicOnlyKeyPair = phalanxService.cloneKeyPairPublic(memberDivisionKeyPair);
        Division<Organization> division = createDivision(associate.getOrganization(), null, publicOnlyKeyPair, slug, name);
        Enlistment enlistment = createEnlistment(associate, protectedBy, division, memberDivisionKeyPair);
        return enlistment;
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public <Owner extends Actor, Target extends Actor> Partnership<Owner, Target> createDivisionPartnership(Division<Owner> source, Target target, String slug, String name) {
        Owner owner = source.getOwner();
        KeyPair newKeyPair = keySafeService.createKeyPair(source);
        KeyPair publicOnlyKeyPair = phalanxService.cloneKeyPairPublic(newKeyPair);
        Division<Target> division = createDivision(target, null, newKeyPair, slug, name);
        Partnership<Owner, Target> partnership = createPartnership(owner, source, division, newKeyPair);
        division.setKeyPairId(publicOnlyKeyPair.getId());
        return partnership;
    }
    
    
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.OrganizationService#createAssociate(org.brekka.pegasus.core.model.Member, org.brekka.pegasus.core.model.KeySafe, java.lang.String, org.brekka.pegasus.core.model.Associate)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Enlistment createEnlistment(Associate toAssign, KeySafe<Member> assignToKeySafe, 
            Connection<Associate, KeySafe<Member>, Division<Organization>> existingEnlistment) {
        Division<Organization> target = existingEnlistment.getTarget();
        KeyPair identityKeyPair = new IdentityKeyPair(existingEnlistment.getKeyPairId());
        KeyPair newKeyPair = keySafeService.assignKeyPair(existingEnlistment.getSource(), identityKeyPair, assignToKeySafe);
        Enlistment enlistment = createEnlistment(toAssign, assignToKeySafe, target, newKeyPair);
        return enlistment;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.DivisionService#createDivision(org.brekka.pegasus.core.model.Division, java.lang.String, java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public <T extends Actor> Division<T> createDivision(Division<T> parent, String slug, String name) {
        IdentityKeyPair identityKeyPair = new IdentityKeyPair(parent.getKeyPairId());
        KeyPair protectedBy = phalanxService.generateKeyPair(identityKeyPair);
        Division<T> division = createDivision(parent.getOwner(), parent, protectedBy, slug, name);
        return division;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.OrganizationService#retrieveDivision(java.lang.String, java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public <T extends Actor> Division<T> retrieveDivision(T organization, String divisionSlug) {
        Division<T> division = divisionDAO.retrieveBySlug(organization, divisionSlug);
        return division;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.OrganizationService#retrieveDivisions(org.brekka.pegasus.core.model.Organization)
     */
    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public List<Enlistment> retrieveCurrentEnlistments() {
        AuthenticatedMemberBase<Member> currentMember = AuthenticatedMemberBase.getCurrent(memberService, Member.class);
        Associate associate = (Associate) currentMember.getActiveActor();
        return enlistmentDAO.retrieveForAssociate(associate);
    }
    
    /**
     * @param associate
     * @param protectedBy
     * @param memberDivisionKeyPair
     * @param division
     * @return
     */
    protected Enlistment createEnlistment(Associate associate, KeySafe<Member> protectedBy, Division<Organization> division, KeyPair memberDivisionKeyPair) {
        Enlistment enlistment = new Enlistment();
        enlistment.setId(UUID.randomUUID());
        enlistment.setAssociate(associate);
        enlistment.setDivision(division);
        enlistment.setSource(protectedBy);
        enlistment.setKeyPairId(memberDivisionKeyPair.getId());
        enlistmentDAO.create(enlistment);
        return enlistment;
    }
    
    protected <T extends Actor> Division<T> createDivision(T owner, Division<T> parent, 
            KeyPair protectedBy, String slug, String name) {
        Division<T> division = new Division<>();
        division.setId(UUID.randomUUID());
        division.setOwner(owner);
        division.setParent(parent);
        if (protectedBy != null) {
            division.setKeyPairId(protectedBy.getId());
        }
        division.setName(name);
        division.setSlug(slug);
        division.setStatus(KeySafeStatus.ACTIVE);
        divisionDAO.create(division);
        return division;
    }
}
