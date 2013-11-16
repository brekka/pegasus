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

import org.brekka.commons.persistence.support.EntityUtils;
import org.brekka.pegasus.core.dao.KeySafeDAO;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.KeySafeService;
import org.brekka.phalanx.api.beans.IdentityCryptedData;
import org.brekka.phalanx.api.beans.IdentityKeyPair;
import org.brekka.phalanx.api.beans.IdentityPrincipal;
import org.brekka.phalanx.api.model.CryptedData;
import org.brekka.phalanx.api.model.KeyPair;
import org.brekka.phalanx.api.model.Principal;
import org.brekka.phalanx.api.model.PrivateKeyToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Operations relating to all {@link KeySafe} based instances.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Service
@Transactional
public class KeySafeServiceImpl extends AbstractKeySafeServiceSupport implements KeySafeService {

    @Autowired
    private KeySafeDAO keySafeDAO;

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.KeySafeService#protect(byte[], org.brekka.pegasus.core.model.KeySafe)
     */
    @Override
    @Transactional()
    public CryptedData protect(final byte[] keyData, final KeySafe<?> keySafe) {
        KeySafe<?> nKeySafe = EntityUtils.narrow(keySafe, KeySafe.class);
        if (nKeySafe == null) {
            throw new IllegalArgumentException("A keySafe must be specified");
        }
        CryptedData cryptedData;
        if (nKeySafe instanceof Vault) {
            Vault vault = (Vault) nKeySafe;
            cryptedData = this.phalanxService.asymEncrypt(keyData,
                    new IdentityPrincipal(vault.getPrincipalId()));
        } else if (nKeySafe instanceof Division) {
            Division<?> division = (Division<?>) nKeySafe;
            cryptedData = this.phalanxService.asymEncrypt(keyData,
                    new IdentityKeyPair(division.getKeyPairId()));
        } else {
            throw new IllegalStateException("Unknown keySafe type: " + nKeySafe.getClass().getName());
        }
        return cryptedData;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.KeySafeService#retrieveById(java.util.UUID)
     */
    @Override
    @Transactional(readOnly=true)
    public KeySafe<? extends Actor> retrieveById(final UUID id) {
        return this.keySafeDAO.retrieveById(id);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.KeySafeService#release(java.util.UUID, org.brekka.pegasus.core.model.KeySafe)
     */
    @Override
    @Transactional(readOnly=true)
    public byte[] release(final UUID cryptedDataId, final KeySafe<?> keySafe) {
        if (keySafe == null) {
            throw new IllegalArgumentException("A keySafe must be specified");
        }
        byte[] data;
        AuthenticatedMemberBase<Member> currentMember = AuthenticatedMemberBase.getCurrent(this.memberService, Member.class);
        PrivateKeyToken privateKey = resolvePrivateKeyFor(keySafe, currentMember);
        data = this.phalanxService.asymDecrypt(new IdentityCryptedData(cryptedDataId), privateKey);
        return data;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.KeySafeService#createKeyPair(org.brekka.pegasus.core.model.KeySafe)
     */
    @Override
    @Transactional()
    public KeyPair createKeyPair(final KeySafe<?> keySafe) {
        return super.createKeyPair(keySafe);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.KeySafeService#assignKeyPair(org.brekka.pegasus.core.model.KeySafe, org.brekka.phalanx.api.model.KeyPair)
     */
    @Override
    @Transactional()
    public KeyPair assignKeyPair(final KeySafe<?> protectingKeySafe, final KeyPair keyPairToAssign, final KeySafe<?> assignToKeySafe) {
        KeySafe<?> nAssignToKeySafe = EntityUtils.narrow(assignToKeySafe, KeySafe.class);
        KeyPair keyPair;
        AuthenticatedMemberBase<Member> currentMember = AuthenticatedMemberBase.getCurrent(this.memberService, Member.class);
        PrivateKeyToken privateKeyToken = resolveAndUnlock(protectingKeySafe, keyPairToAssign, currentMember);

        if (nAssignToKeySafe instanceof Vault) {
            Vault vault = (Vault) nAssignToKeySafe;
            Principal identityPrincipal = new IdentityPrincipal(vault.getPrincipalId());
            keyPair = this.phalanxService.assignKeyPair(privateKeyToken, identityPrincipal);
        } else if (nAssignToKeySafe instanceof Division) {
            Division<?> division = (Division<?>) nAssignToKeySafe;
            KeyPair identityKeyPair = new IdentityKeyPair(division.getKeyPairId());
            keyPair = this.phalanxService.assignKeyPair(privateKeyToken, identityKeyPair);
        } else {
            throw new IllegalStateException("Unknown assignment keySafe type: " + nAssignToKeySafe.getClass().getName());
        }
        return keyPair;
    }

}
