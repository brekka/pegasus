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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.dao.VaultDAO;
import org.brekka.pegasus.core.event.VaultDeleteEvent;
import org.brekka.pegasus.core.event.VaultOpenEvent;
import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.VaultService;
import org.brekka.pegasus.core.utils.SlugUtils;
import org.brekka.phalanx.api.PhalanxException;
import org.brekka.phalanx.api.beans.IdentityCryptedData;
import org.brekka.phalanx.api.beans.IdentityPrincipal;
import org.brekka.phalanx.api.model.AuthenticatedPrincipal;
import org.brekka.phalanx.api.model.CryptedData;
import org.brekka.phalanx.api.model.KeyPair;
import org.brekka.phalanx.api.model.Principal;
import org.brekka.phalanx.api.model.PrivateKeyToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for manipulating {@link Vault} instances.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Service
@Transactional
public class VaultServiceImpl extends AbstractKeySafeServiceSupport implements VaultService {

    @Autowired
    private VaultDAO vaultDAO;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.VaultService#createVault(org.brekka.pegasus.core.model.Member, java.lang.String)
     */
    @Override
    @Transactional()
    public Vault createVault(final String name, final String vaultPassword, final Member owner) {
        Vault vault = new Vault();
        vault.setOwner(owner);
        vault.setName(name);
        vault.setSlug(SlugUtils.sluggify(name));

        Principal principal = phalanxService.createPrincipal(vaultPassword);
        vault.setPrincipalId(principal.getId());

        vaultDAO.create(vault);
        return vault;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.VaultService#deleteVault(org.brekka.pegasus.core.model.Vault)
     */
    @Override
    @Transactional()
    public void deleteVault(final Vault vault) {
        applicationEventPublisher.publishEvent(new VaultDeleteEvent(vault));
        vaultDAO.delete(vault.getId());
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.VaultService#retrieveById(java.util.UUID)
     */
    @Override
    @Transactional(readOnly=true)
    public Vault retrieveById(final UUID vaultId) {
        return vaultDAO.retrieveById(vaultId);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.VaultService#retrieveForUser()
     */
    @Override
    @Transactional(readOnly=true)
    public List<Vault> retrieveForUser() {
        AuthenticatedMember<Member> current = memberService.getCurrent(Member.class);
        List<Vault> vaultList = vaultDAO.retrieveForMember(current.getMember());
        final Vault defaultVault = current.getMember().getDefaultVault();
        // Sort so that the default appears first, then the rest by name
        Collections.sort(vaultList, new Comparator<Vault>() {
            /* (non-Javadoc)
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            @Override
            public int compare(final Vault o1, final Vault o2) {
                if (o1.getId().equals(defaultVault.getId())) {
                    return -1;
                }
                return o1.getName().compareTo(o2.getName());
            }
        });
        return vaultList;
    }

    @Override
    @Transactional()
    public Vault openVault(final UUID vaultId, final String vaultPassword) {
        Vault managed = retrieveById(vaultId);
        UUID principalId = managed.getPrincipalId();
        AuthenticatedPrincipal authenticatedPrincipal;
        try {
            authenticatedPrincipal = phalanxService.authenticate(new IdentityPrincipal(principalId), vaultPassword);
        } catch (PhalanxException e) {
            throw new PegasusException(PegasusErrorCode.PG302, e, "Unable to unlock vault '%s'", managed.getId());
        }
        managed.setAuthenticatedPrincipal(authenticatedPrincipal);

        if (AuthenticatedMemberBase.isAvailable(memberService)) {
            AuthenticatedMemberBase<Member> currentMember = AuthenticatedMemberBase.getCurrent(memberService, Member.class);
            currentMember.retainVaultKey(managed);

            // Only pulish an open event if it is the current user opening the vault.
            applicationEventPublisher.publishEvent(new VaultOpenEvent(managed));
        }
        return managed;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.VaultService#releaseKey(java.util.UUID, org.brekka.pegasus.core.model.OpenVault)
     */
    @Override
    @Transactional(readOnly=true)
    public byte[] releaseKey(final UUID cryptedDataId, final Vault vault) {
        AuthenticatedPrincipal authenticatedPrincipal = getVaultKey(vault);
        CryptedData cryptedData = new IdentityCryptedData(cryptedDataId);
        PrivateKeyToken privateKey = authenticatedPrincipal.getDefaultPrivateKey();
        byte[] secretKeyBytes = phalanxService.asymDecrypt(cryptedData, privateKey);
        return secretKeyBytes;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.VaultService#createKeyPair(org.brekka.pegasus.core.model.Vault)
     */
    @Override
    @Transactional()
    public KeyPair createKeyPair(final Vault vault) {
        AuthenticatedPrincipal authenticatedPrincipal = getVaultKey(vault);
        KeyPair keyPair = authenticatedPrincipal.getDefaultPrivateKey().getKeyPair();
        KeyPair newKeyPair = phalanxService.generateKeyPair(keyPair, authenticatedPrincipal.getPrincipal());
        return newKeyPair;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.VaultService#releaseKeyPair(org.brekka.phalanx.api.model.KeyPair, org.brekka.pegasus.core.model.Vault)
     */
    @Override
    @Transactional(readOnly=true)
    public PrivateKeyToken releaseKeyPair(final KeyPair keyPair, final Vault vault) {
        AuthenticatedPrincipal authenticatedPrincipal = getVaultKey(vault);
        PrivateKeyToken privateKey = authenticatedPrincipal.getDefaultPrivateKey();
        PrivateKeyToken releasedPrivateKey = phalanxService.decryptKeyPair(keyPair, privateKey);
        return releasedPrivateKey;
    }


    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.VaultService#retrieveBySlug(java.lang.String)
     */
    @Override
    @Transactional(readOnly=true)
    public Vault retrieveBySlug(final String vaultSlug) {
        AuthenticatedMember<Member> current = memberService.getCurrent(Member.class);
        Member member = current.getMember();
        Vault vault = vaultDAO.retrieveBySlug(vaultSlug, member);
        return vault;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.VaultService#isOpen(org.brekka.pegasus.core.model.Vault)
     */
    @Override
    @Transactional(readOnly=true)
    public boolean isOpen(final Vault vault) {
        if (vault == null) {
            return false;
        }
        AuthenticatedMemberBase<Member> currentMember = AuthenticatedMemberBase.getCurrent(memberService, Member.class);
        AuthenticatedPrincipal vaultKey = currentMember.getVaultKey(vault);
        return (vaultKey != null);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.VaultService#changePassword(org.brekka.pegasus.core.model.Vault, java.lang.String, java.lang.String)
     */
    @Override
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void changePassword(final Vault defaultVault, final String oldPassword, final String newPassword) {
        UUID principalId = defaultVault.getPrincipalId();
        phalanxService.changePassword(new IdentityPrincipal(principalId), oldPassword, newPassword);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.VaultService#changePassword(org.brekka.pegasus.core.model.Vault, java.lang.String)
     */
    @Override
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void changePassword(final Vault defaultVault, final String password) {
        Vault managed = vaultDAO.retrieveById(defaultVault.getId());
        // Replace the principal. The old principal will just be orphaned.
        Principal principal = phalanxService.createPrincipal(password);
        managed.setPrincipalId(principal.getId());
        vaultDAO.update(managed);
        defaultVault.setPrincipalId(managed.getPrincipalId());
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.VaultService#closeVault(java.util.UUID)
     */
    @Override
    @Transactional()
    public void closeVault(final UUID vaultId) {
        AuthenticatedMemberBase<Member> currentMember = AuthenticatedMemberBase.getCurrent(memberService, Member.class);
        Vault vault = new Vault(vaultId);
        AuthenticatedPrincipal authenticatedPrincipal = currentMember.getVaultKey(vault);
        phalanxService.logout(authenticatedPrincipal);
        currentMember.clearVault(vault);
    }
}
