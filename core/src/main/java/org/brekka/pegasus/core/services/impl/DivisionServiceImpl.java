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

import org.brekka.pegasus.core.dao.DivisionDAO;
import org.brekka.pegasus.core.dao.EnlistmentDAO;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.Enlistment;
import org.brekka.pegasus.core.model.KeySafeStatus;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Vault;
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
    public Enlistment createRootDivision(Associate associate, Vault connectedTo, String slug, String name) {
        KeyPair associateDivisionKeyPair = vaultService.createKeyPair(connectedTo);
        
        KeyPair anonKeyPair = phalanxService.cloneKeyPairPublic(associateDivisionKeyPair);
        
        Division<Organization> division = createDivision(associate.getOrganization(), null, anonKeyPair, slug, name);
        
        Enlistment enlistment = new Enlistment();
        enlistment.setAssociate(associate);
        enlistment.setDivision(division);
        enlistment.setSource(connectedTo);
        enlistment.setKeyPairId(associateDivisionKeyPair.getId());
        enlistmentDAO.create(enlistment);
        
        return enlistment;
    }
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public <T extends Actor> Division<T> createRootDivision(T owner, KeyPair protectedByKeyPair, String slug, String name) {
        KeyPair rootDivisionKeyPair = phalanxService.generateKeyPair(protectedByKeyPair);
        Division<T> division = createDivision(owner, null, rootDivisionKeyPair, slug, name);
        return division;
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
    
    protected <T extends Actor> Division<T> createDivision(T owner, Division<T> parent, 
            KeyPair protectedBy, String slug, String name) {
       
        Division<T> division = new Division<>();
        division.setOwner(owner);
        division.setParent(parent);
        division.setKeyPairId(protectedBy.getId());
        division.setName(name);
        division.setSlug(slug);
        division.setStatus(KeySafeStatus.ACTIVE);
        divisionDAO.create(division);
        return division;
    }
}
