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

import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.dao.DivisionDAO;
import org.brekka.pegasus.core.dao.EnlistmentDAO;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.Connection;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.Enlistment;
import org.brekka.pegasus.core.model.Fallback;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.KeySafeStatus;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Partnership;
import org.brekka.pegasus.core.model.Person;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.DivisionService;
import org.brekka.pegasus.core.services.KeySafeService;
import org.brekka.pegasus.core.services.VaultService;
import org.brekka.phalanx.api.beans.IdentityKeyPair;
import org.brekka.phalanx.api.model.KeyPair;
import org.brekka.phalanx.api.model.PrivateKeyToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Division Service 
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
    @Transactional()
    public Enlistment createDivisionEnlistment(Associate associate, KeySafe<? extends Member> protectedBy, String slug, String name) {
        KeyPair memberDivisionKeyPair = keySafeService.createKeyPair(protectedBy);
        KeyPair publicOnlyKeyPair = phalanxService.cloneKeyPairPublic(memberDivisionKeyPair);
        Division<Organization> division = createDivision(associate.getOrganization(), null, publicOnlyKeyPair, slug, name);
        Enlistment enlistment = createEnlistment(associate, protectedBy, division, memberDivisionKeyPair);
        return enlistment;
    }

    @Override
    @Transactional()
    public <Owner extends Actor, Target extends Actor> Partnership<Owner, Target> createDivisionPartnership(Division<Owner> source, Target target, String slug, String name) {
        Owner owner = source.getOwner();
        KeyPair newKeyPair = keySafeService.createKeyPair(source);
        KeyPair publicOnlyKeyPair = phalanxService.cloneKeyPairPublic(newKeyPair);
        Division<Target> division = createDivision(target, null, newKeyPair, slug, name);
        Partnership<Owner, Target> partnership = new Partnership<>();
        partnership = createConnection(partnership, owner, source, division, newKeyPair);
        division.setKeyPairId(publicOnlyKeyPair.getId());
        return partnership;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.impl.AbstractKeySafeServiceSupport#createPartnership(org.brekka.pegasus.core.model.Actor, org.brekka.pegasus.core.model.Division, org.brekka.pegasus.core.model.Division, org.brekka.phalanx.api.model.KeyPair)
     */
    @Override
    @Transactional()
    public <Owner extends Actor, Target extends Actor> Partnership<Owner, Target> createPartnership(Owner owner,
            Division<Owner> source, Division<Target> target) {
        Partnership<Owner, Target> partnership = new Partnership<>();
        return createConnection(partnership, owner, source, target);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.impl.AbstractKeySafeServiceSupport#createPartnership(org.brekka.pegasus.core.model.Actor, org.brekka.pegasus.core.model.Division, org.brekka.pegasus.core.model.Division, org.brekka.phalanx.api.model.KeyPair)
     */
    @Override
    @Transactional()
    public <Owner extends Actor> Fallback<Owner> createFallback(Owner owner,
            Division<Owner> source, Division<Person> target) {
        Fallback<Owner> fallback = new Fallback<>();
        return createConnection(fallback, owner, source, target);
    }
    
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.DivisionService#retrievePartnershipById(java.util.UUID)
     */
    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly=true)
    public <Owner extends Actor, Target extends Actor> Partnership<Owner, Target> retrievePartnershipById(
            UUID partnershipId) {
        return (Partnership<Owner, Target>) connectionDAO.retrieveById(partnershipId);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.impl.DivisionServiceImpl#createEnlistment(org.brekka.pegasus.core.model.Associate, org.brekka.pegasus.core.model.KeySafe, org.brekka.pegasus.core.model.Connection)
     */
    @Override
    @Transactional()
    public <Owner extends Actor, Source extends KeySafe<?>> Enlistment createEnlistment(Associate toAssign, KeySafe<? extends Member> assignToKeySafe, 
            Connection<Owner, Source, Division<Organization>> existingEnlistment) {
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
    @Transactional()
    public <T extends Actor> Division<T> createDivision(KeySafe<T> parent, String slug, String name) {
        KeyPair divisionKeyPair;
        PrivateKeyToken privateKey = null;
        if (parent instanceof Division) {
            Division<T> division = (Division<T>) parent;
            IdentityKeyPair identityKeyPair = new IdentityKeyPair(division.getKeyPairId());
            divisionKeyPair = phalanxService.generateKeyPair(identityKeyPair);
        } else if (parent instanceof Vault) {
            Vault vault = (Vault) parent;
            KeyPair keyPair = vault.getAuthenticatedPrincipal().getPrincipal().getDefaultKeyPair();
            divisionKeyPair = phalanxService.generateKeyPair(keyPair);
            privateKey = phalanxService.decryptKeyPair(divisionKeyPair, vault.getAuthenticatedPrincipal().getDefaultPrivateKey());
        } else {
            throw new PegasusException(PegasusErrorCode.PG701, 
                    "Unable to handle keySafe type '%s' at this location", parent.getClass().getName());
        }
        Division<T> division = createDivision(parent.getOwner(), parent, divisionKeyPair, slug, name);
        division.setPrivateKeyToken(privateKey);
        return division;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.OrganizationService#retrieveDivision(java.lang.String, java.lang.String)
     */
    @Override
    @Transactional(readOnly=true)
    public <T extends Actor> Division<T> retrieveDivision(T organization, String divisionSlug) {
        Division<T> division = divisionDAO.retrieveBySlug(organization, divisionSlug);
        return division;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.OrganizationService#retrieveDivisions(org.brekka.pegasus.core.model.Organization)
     */
    @Transactional(readOnly=true)
    @Override
    public List<Enlistment> retrieveCurrentEnlistments() {
        AuthenticatedMemberBase<Member> currentMember = AuthenticatedMemberBase.getCurrent(memberService, Member.class);
        Associate associate = (Associate) currentMember.getActiveActor();
        return enlistmentDAO.retrieveForAssociate(associate);
    }
    
    /**
     * Replace the keyPair in the division with that backed-up to a fallback identified by the division as the target.
     * 
     * @param division
     *            the division being restored
     * @param protectWith
     *            the keySafe to assign access to the keyPair that must have been previously backed up via a
     *            partnership.
     */
    @Transactional(isolation=Isolation.SERIALIZABLE)
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Actor> void restoreDivision(Division<T> division, KeySafe<?> protectWith) {
        List<Fallback<?>> fallbacks = connectionDAO.retrieveConnectionsByTarget(division, Fallback.class);
        
        if (fallbacks.size() != 1) {
            throw new PegasusException(PegasusErrorCode.PG723, 
                    "Unable to find backup of division '%s'", division.getId());
        }
        
        for (Fallback<?> partnership : fallbacks) {
            Division<?> globalDivision = partnership.getSource();
            KeyPair keyPair = new IdentityKeyPair(partnership.getKeyPairId());
            KeyPair updatedKeyPair = keySafeService.assignKeyPair(globalDivision, keyPair, protectWith);
            division.setKeyPairId(updatedKeyPair.getId());
            
            Division<?> managed = divisionDAO.retrieveById(division.getId());
            managed.setKeyPairId(updatedKeyPair.getId());
            divisionDAO.update(managed);
        }
    }
    
    protected <Owner extends Actor, Source extends Division<? extends Actor>, 
                Target extends Division<?>, T extends Connection< Owner, Source, Target >> 
            T createConnection(T connection, Owner owner, Source source, Target target) {
        PrivateKeyToken privateKeyToken = target.getPrivateKeyToken();
        if (privateKeyToken == null) {
            AuthenticatedMemberBase<Member> currentMember = AuthenticatedMemberBase.getCurrent(memberService,
                    Member.class);
            privateKeyToken = resolvePrivateKeyFor(target, currentMember);
        }
        IdentityKeyPair sourceKeyPair = new IdentityKeyPair(source.getKeyPairId());
        KeyPair connectionKeyPair = phalanxService.assignKeyPair(privateKeyToken, sourceKeyPair);
        return super.createConnection(connection, owner, source, target, connectionKeyPair);
    }
    
    /**
     * @param associate
     * @param protectedBy
     * @param memberDivisionKeyPair
     * @param division
     * @return
     */
    protected Enlistment createEnlistment(Associate associate, KeySafe<? extends Member> protectedBy, Division<Organization> division, KeyPair memberDivisionKeyPair) {
        Enlistment enlistment = new Enlistment();
        enlistment.setId(UUID.randomUUID());
        enlistment.setAssociate(associate);
        enlistment.setDivision(division);
        enlistment.setSource(protectedBy);
        enlistment.setKeyPairId(memberDivisionKeyPair.getId());
        enlistmentDAO.create(enlistment);
        return enlistment;
    }
    
    protected <T extends Actor> Division<T> createDivision(T owner, KeySafe<T> parent, 
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
