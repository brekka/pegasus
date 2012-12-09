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
import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.DivisionAssociate;
import org.brekka.pegasus.core.model.KeySafeStatus;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.DivisionService;
import org.brekka.pegasus.core.services.KeySafeService;
import org.brekka.pegasus.core.services.VaultService;
import org.brekka.phalanx.api.beans.IdentityKeyPair;
import org.brekka.phalanx.api.beans.IdentityPrincipal;
import org.brekka.phalanx.api.model.KeyPair;
import org.brekka.phalanx.api.model.PrivateKeyToken;
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
    
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.OrganizationService#createDivision(org.brekka.pegasus.core.model.Organization, java.lang.String, java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public DivisionAssociate createRootDivision(Associate associate, Vault connectedTo, String slug, String name) {
        KeyPair associateDivisionKeyPair = vaultService.createKeyPair(connectedTo);
        
        KeyPair anonKeyPair = phalanxService.cloneKeyPairPublic(associateDivisionKeyPair);
        
        Division division = createDivision(associate.getOrganization(), null, anonKeyPair, slug, name);
        
        DivisionAssociate divisionAssociate = new DivisionAssociate();
        divisionAssociate.setAssociate(associate);
        divisionAssociate.setDivision(division);
        divisionAssociate.setKeyPairId(associateDivisionKeyPair.getId());
        divisionAssociateDAO.create(divisionAssociate);
        
        return divisionAssociate;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.DivisionService#createDivision(org.brekka.pegasus.core.model.Division, java.lang.String, java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Division createDivision(Division parent, String slug, String name) {
        IdentityKeyPair identityKeyPair = new IdentityKeyPair(parent.getKeyPairId());
        KeyPair protectedBy = phalanxService.generateKeyPair(identityKeyPair);
        Division division = createDivision(parent.getOrganization(), parent, protectedBy, slug, name);
        return division;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.OrganizationService#retrieveDivision(java.lang.String, java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Division retrieveDivision(Organization organization, String divisionSlug) {
        Division division = divisionDAO.retrieveBySlug(organization, divisionSlug);
        return division;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.OrganizationService#retrieveDivisions(org.brekka.pegasus.core.model.Organization)
     */
    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public List<DivisionAssociate> retrieveCurrentDivisions() {
        AuthenticatedMemberBase<Member> currentMember = AuthenticatedMemberBase.getCurrent(memberService, Member.class);
        Associate associate = (Associate) currentMember.getActiveActor();
        return divisionAssociateDAO.retrieveForOrg(associate);
    }
    
    protected Division createDivision(Organization organization, Division parent, 
            KeyPair protectedBy, String slug, String name) {
       
        Division division = new Division();
        division.setOrganization(organization);
        division.setParent(parent);
        division.setKeyPairId(protectedBy.getId());
        division.setName(name);
        division.setSlug(slug);
        division.setStatus(KeySafeStatus.ACTIVE);
        divisionDAO.create(division);
        return division;
    }
}
