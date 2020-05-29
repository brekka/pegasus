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

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.model.AccessorContext;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.MemberContext;
import org.brekka.pegasus.core.model.Profile;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.model.XmlEntity;
import org.brekka.pegasus.core.utils.EntityUnlockKeyCache;
import org.brekka.phalanx.api.model.AuthenticatedPrincipal;
import org.brekka.phalanx.api.model.KeyPair;
import org.brekka.phalanx.api.model.PrivateKeyToken;
import org.brekka.xml.pegasus.v2.model.ProfileDocument;
import org.brekka.xml.pegasus.v2.model.ProfileType;
import org.springframework.security.core.userdetails.UserDetails;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Retains the details for a logged-in member. It forms the basis of the {@link UserDetails} that is bound to the security
 * context for a given user.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
class MemberContextImpl implements MemberContext {

    /**
     * The collection of Phalanx vault key references that are currently unlocked.
     *
     * A non-expiring 'cache' of the unlocked vault keys in memory.
     */
    private final EntityUnlockKeyCache<AuthenticatedPrincipal> vaultKeyCache = new EntityUnlockKeyCache<>(HashMap::new);

    /**
     * Collection of unlocked Phalanx private key references.
     */
    private final EntityUnlockKeyCache<PrivateKeyToken> privateKeyCache = new EntityUnlockKeyCache<>(() -> Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .maximumSize(200)
            .<UUID, PrivateKeyToken>build()
            .asMap());


    /**
     * Essentially a cache of expensive to generate values (non-serializable).
     */
    private final AccessorContext accessorContext = new AccessorContextImpl();

    /**
     * Which vault is currently active (unlocked)
     */
    private Vault activeVault;

    /**
     * Will always be the member instance that corresponds to the login.
     */
    private Member member;

    /**
     * Determines which actor is active. Normally this will be the same as 'person' but the user
     * can switch 'context' to for instance their associate entry.
     */
    private Actor activeActor;


    /**
     * The user-selected active profile
     */
    private Profile activeProfile;

    public MemberContextImpl(final Member member) {
        this.member = member;
        setActiveActor(member);
    }

    @Override
    public Actor getActiveActor() {
        return this.activeActor;
    }

    @Override
    public Vault getActiveVault() {
        return activeVault;
    }

    @Override
    public ProfileType getProfile() {
        if (activeProfile == null
                || activeProfile.getId() == null) {
            return null;
        }
        XmlEntity<ProfileDocument> xmlEntity = activeProfile.getXml();
        ProfileDocument bean = xmlEntity.getBean();
        if (bean == null) {
            return null;
        }
        return bean.getProfile();
    }

    void setMember(final Member member) {
        this.member = member;
    }

    void setActiveProfile(final Profile activeProfile) {
        this.activeProfile = activeProfile;
    }

    void setActiveVault(final Vault activeVault) {
        this.activeVault = activeVault;
    }

    @Override
    public Profile getActiveProfile() {
        return this.activeProfile;
    }

    @Override
    public AuthenticatedPrincipal getVaultKey(final Vault vault) {
        return this.vaultKeyCache.get(vault.getId());
    }

    @Override
    public void retainVaultKey(final Vault vault) {
        this.vaultKeyCache.put(vault.getId(), vault.getAuthenticatedPrincipal());
    }

    @Override
    public PrivateKeyToken getPrivateKey(final KeyPair keyPair) {
        return this.privateKeyCache.get(keyPair.getId());
    }

    @Override
    public void retainPrivateKey(final KeyPair keyPair, final PrivateKeyToken privateKeyToken) {
        if (!keyPair.getId().equals(privateKeyToken.getKeyPair().getId())) {
            throw new PegasusException(PegasusErrorCode.PG104,
                    "Private key token does not belong to keyPair '%s'. It instead belongs to '%s'",
                    keyPair.getId(), privateKeyToken.getKeyPair().getId());
        }
        this.privateKeyCache.put(keyPair.getId(), privateKeyToken);
    }

    @Override
    public void setActiveActor(final Actor activeActor) {
        this.activeActor = activeActor;
    }

    synchronized List<AuthenticatedPrincipal> clearVaults() {
        return this.vaultKeyCache.clear();
    }

    @Override
    public synchronized void clearVault(final Vault vault) {
        this.vaultKeyCache.remove(vault.getId());
    }

    @Override
    public AccessorContext getAccessorContext() {
        return this.accessorContext;
    }

    @Override
    public Member getMember() {
        return this.member;
    }
}
