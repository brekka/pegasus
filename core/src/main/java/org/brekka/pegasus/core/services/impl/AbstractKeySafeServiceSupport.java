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

import java.util.UUID;

import org.brekka.pegasus.core.dao.DivisionAssociateDAO;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.DivisionAssociate;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.phalanx.api.beans.IdentityKeyPair;
import org.brekka.phalanx.api.model.AuthenticatedPrincipal;
import org.brekka.phalanx.api.model.KeyPair;
import org.brekka.phalanx.api.model.PrivateKeyToken;
import org.brekka.phalanx.api.services.PhalanxService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO Description of AbstractKeySafeServiceSupport
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
abstract class AbstractKeySafeServiceSupport {
  
    
    @Autowired
    protected PhalanxService phalanxService;
    
    @Autowired
    protected MemberService memberService;
    
    @Autowired
    protected DivisionAssociateDAO  divisionAssociateDAO;
    
    
    /**
     * @param division
     * @param currentMember
     * @return
     */
    protected PrivateKeyToken identifyPrivateKey(Division division, AuthenticatedMemberBase<?> currentMember) {
        Actor activeActor = currentMember.getActiveActor();
        if (activeActor instanceof Associate == false) {
            // TODO
            throw new IllegalStateException();
        }
        Associate associate = (Associate) activeActor;
        return resolvePrivateKeyFor(division, associate, currentMember);
    }
    
    protected PrivateKeyToken resolvePrivateKeyFor(Division division, Associate associate, AuthenticatedMemberBase<?> currentMember) {
        PrivateKeyToken privateKeyToken;
        DivisionAssociate divisionAssociate = divisionAssociateDAO.retrieveBySurrogateKey(division, associate);
        if (divisionAssociate != null) {
            privateKeyToken = currentMember.getPrivateKey(divisionAssociate.getId());
            if (privateKeyToken == null) {
                UUID keyPairId = divisionAssociate.getKeyPairId();
                Vault vault = divisionAssociate.getVault();
                AuthenticatedPrincipal vaultKey = currentMember.getVaultKey(vault.getId());
                PrivateKeyToken userPrivateKey = vaultKey.getDefaultPrivateKey();
                privateKeyToken = phalanxService.decryptKeyPair(new IdentityKeyPair(keyPairId), userPrivateKey);
                currentMember.retainPrivateKey(divisionAssociate.getId(), privateKeyToken);
            }
        } else {
            if (division.getParent() != null) {
                privateKeyToken = currentMember.getPrivateKey(division.getId());
                if (privateKeyToken == null) {
                    PrivateKeyToken parentPrivateKeyToken = resolvePrivateKeyFor(division.getParent(), associate, currentMember);
                    UUID keyPairId = division.getKeyPairId();
                    privateKeyToken = phalanxService.decryptKeyPair(new IdentityKeyPair(keyPairId), parentPrivateKeyToken);
                    currentMember.retainPrivateKey(division.getId(), privateKeyToken);
                }
            } else {
                throw new IllegalStateException("The user does not have access to this division");
            }
        }
        return privateKeyToken;
    }
    
    protected PrivateKeyToken unlockPrivateKey(KeyPair keyPair, Vault vault, AuthenticatedMemberBase<?> currentMember) {
        AuthenticatedPrincipal vaultKey = currentMember.getVaultKey(vault.getId());
        PrivateKeyToken userPrivateKey = vaultKey.getDefaultPrivateKey();
        PrivateKeyToken privateKeyToken = phalanxService.decryptKeyPair(new IdentityKeyPair(keyPair.getId()), userPrivateKey);
        return privateKeyToken;
    }
}
