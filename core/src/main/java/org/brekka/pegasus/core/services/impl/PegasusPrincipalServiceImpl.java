/*
 * Copyright 2016 the original author or authors.
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

import static org.brekka.commons.persistence.support.EntityUtils.narrow;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.dao.AuthenticationTokenDAO;
import org.brekka.pegasus.core.dao.MemberDAO;
import org.brekka.pegasus.core.event.VaultOpenEvent;
import org.brekka.pegasus.core.model.ActorStatus;
import org.brekka.pegasus.core.model.AuthenticationToken;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.MemberContext;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Profile;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.security.PegasusPrincipal;
import org.brekka.pegasus.core.security.PegasusPrincipalAware;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.PegasusPrincipalService;
import org.brekka.pegasus.core.services.ProfileService;
import org.brekka.pegasus.core.services.VaultService;
import org.brekka.phalanx.api.model.AuthenticatedPrincipal;
import org.brekka.phalanx.api.model.ExportedPrincipal;
import org.brekka.phalanx.api.services.PhalanxService;
import org.brekka.phoenix.api.services.RandomCryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PegasusPrincipalServiceImpl implements PegasusPrincipalService {

    private static final Log log = LogFactory.getLog(PegasusPrincipalServiceImpl.class);

    @Autowired
    private MemberDAO memberDAO;

    @Autowired
    private AuthenticationTokenDAO authenticationTokenDAO;

    @Autowired
    private RandomCryptoService randomCryptoService;

    @Autowired
    private PhalanxService phalanxService;

    @Autowired
    private VaultService vaultService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    private final ThreadLocal<PegasusPrincipalImpl> threadLocalPrincipals = new ThreadLocal<>();

    @Override
    public PegasusPrincipal currentPrincipal(final boolean required) {
        PegasusPrincipal pegasusPrincipal = threadLocalPrincipals.get();
        if (pegasusPrincipal != null) {
            return pegasusPrincipal;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof PegasusPrincipalAware) {
                PegasusPrincipalAware tokenSource = (PegasusPrincipalAware) principal;
                pegasusPrincipal = tokenSource.getPegasusPrincipal();
            }
        }
        if (pegasusPrincipal == null && required) {
            throw new PegasusException(PegasusErrorCode.PG902,
                    "No pegasus principal could be found in the current security context");
        }
        return pegasusPrincipal;
    }



    @Override
    @Transactional
    public PegasusPrincipal login(final Member member, final Organization organization, final String vaultPassword) {
        PegasusPrincipalImpl pegasusPrincipal = new PegasusPrincipalImpl(member.getAuthenticationToken());
        loginInternal(pegasusPrincipal, vaultPassword, organization, member, null, false, false);
        return pegasusPrincipal;
    }

    @Override
    @Transactional
    public PegasusPrincipal login(final Member member, final Organization organization, final Vault vault,
            final String vaultPassword) {

        PegasusPrincipalImpl pegasusPrincipal = new PegasusPrincipalImpl(member.getAuthenticationToken());
        loginInternal(pegasusPrincipal, vaultPassword, organization, member, vault, false, false);
        return pegasusPrincipal;
    }

    @Override
    @Transactional
    public ExportedPrincipal exportPrincipal(final PegasusPrincipal principal, final byte[] restoreSecret) {
        Vault activeVault = principal.getMemberContext().getActiveVault();
        AuthenticatedPrincipal authenticatedPrincipal = activeVault.getAuthenticatedPrincipal();
        ExportedPrincipal exportedPrincipal = phalanxService.exportPrincipal(authenticatedPrincipal, restoreSecret);
        return exportedPrincipal;
    }

    @Override
    @Transactional
    public PegasusPrincipal restorePrincipal(final Member member, final Organization organization,
            final ExportedPrincipal exportedPrincipal, final byte[] restoreSecret) {

        AuthenticatedPrincipal importedPrincipal = phalanxService.importPrincipal(exportedPrincipal, restoreSecret);
        PegasusPrincipalImpl pegasusPrincipal = new PegasusPrincipalImpl(member.getAuthenticationToken());
        doWithPrincipal(pegasusPrincipal, () -> {
            Vault vault = member.getDefaultVault();
            vault = narrow(vault, Vault.class);
            vault.setAuthenticatedPrincipal(importedPrincipal);
            restore(pegasusPrincipal, member, organization, vault);
        });
        return pegasusPrincipal;
    }

    @Override
    @Transactional()
    public void logout(final PegasusPrincipal pegasusPrincipal) {
        synchronized (pegasusPrincipal) {
            MemberContextImpl memberContext = (MemberContextImpl) pegasusPrincipal.getMemberContext();
            StopWatch sw = new StopWatch();
            if (memberContext != null) {
                List<AuthenticatedPrincipal> authenticatedPrincipals = memberContext.clearVaults();
                for (AuthenticatedPrincipal authenticatedPrincipal : authenticatedPrincipals) {
                    this.phalanxService.logout(authenticatedPrincipal);
                }
            }
            ((PegasusPrincipalImpl) pegasusPrincipal).setMemberContext(null);
            if (log.isInfoEnabled()) {
                log.info(String.format("Pegasus logout for '%s' took %d ms",
                        pegasusPrincipal.getName(), sw.getTime()));
            }
        }
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void loginAndBind(final PegasusPrincipalAware principalSource, final String password,
            final Organization organization, final boolean restoreRequired) {
        PegasusPrincipalImpl pegasusPrincipal = (PegasusPrincipalImpl) principalSource.getPegasusPrincipal();
        loginInternal(pegasusPrincipal, password, organization, null, null, restoreRequired, true);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void loginAndBind(final PegasusPrincipalAware principalSource, final String password,
            final Organization organization, final Member member, final Vault vault) {
        PegasusPrincipalImpl pegasusPrincipal = (PegasusPrincipalImpl) principalSource.getPegasusPrincipal();
        loginInternal(pegasusPrincipal, password, organization, member, vault, false, true);
    }


    private void loginInternal(final PegasusPrincipalImpl pegasusPrincipal, final String password,
            final Organization organization, final Member memberIn, final Vault vaultIn, final boolean restoreRequired,
            final boolean bind) {

        StopWatch sw = new StopWatch();
        synchronized (pegasusPrincipal) {
            if (pegasusPrincipal.getMemberContext() != null) {
                return;
            }
            try {
                Member member;
                if (memberIn == null) {
                    AuthenticationToken authenticationToken = authenticationTokenDAO
                            .retrieveById(pegasusPrincipal.getAuthenticationTokenId());
                    member = memberDAO.retrieveByAuthenticationToken(authenticationToken);
                } else {
                    member = memberDAO.retrieveById(memberIn.getId());
                }
                if (member.getStatus() != ActorStatus.ACTIVE) {
                    throw new DisabledException(String.format("Member '%s' account is disabled", member.getId()));
                }
                Vault vault = vaultIn;
                if (vault == null) {
                    vault = member.getDefaultVault();
                }
                member = narrow(member, Member.class);
                MemberContextImpl memberContext = new MemberContextImpl(member);
                Profile activeProfile = profileService.retrieveProfile(member);
                memberContext.setActiveProfile(activeProfile != null ? activeProfile : new Profile());
                memberContext.setActiveVault(vault);
                // Vault service will use this
                pegasusPrincipal.setMemberContext(memberContext);
                if (bind) {
                    threadLocalPrincipals.set(pegasusPrincipal);
                }

                UUID vaultId = vault.getId();
                vault = doWithPrincipal(pegasusPrincipal, () -> vaultService.openVault(vaultId, password));
                vault = narrow(vault, Vault.class);
                member.setDefaultVault(vault);
                if (organization != null) {
                    doWithPrincipal(pegasusPrincipal, () -> memberService.activateOrganization(organization));
                }
                if (restoreRequired) {
                    prepareRestore(pegasusPrincipal, vault);
                }
                pegasusPrincipal.setOrganizationId(organization != null ? organization.getId() : null);
                if (log.isInfoEnabled()) {
                    log.info(String.format("Pegasus login for '%s' with organization '%s', took %d ms",
                            pegasusPrincipal.getName(), organization != null ? organization.getName() : null,
                            sw.getTime()));
                }
            } catch (RuntimeException e) {
                // We need to bind early to support the latter restore operations, however if an error occurs, make sure
                // to unbind the broken context.
                pegasusPrincipal.setMemberContext(null);
                throw e;
            }
        }
    }

    @Override
    public void unbind() {
        threadLocalPrincipals.remove();
    }

    @Override
    public void doWithPrincipal(final PegasusPrincipal principal, final Runnable runnable) {
        PegasusPrincipalImpl previous = threadLocalPrincipals.get();
        try {
            threadLocalPrincipals.set((PegasusPrincipalImpl) principal);
            runnable.run();
        } finally {
            threadLocalPrincipals.set(previous);
        }
    }

    @Override
    public <T> T doWithPrincipal(final PegasusPrincipal principal, final Supplier<T> supplier) {
        PegasusPrincipalImpl previous = threadLocalPrincipals.get();
        try {
            threadLocalPrincipals.set((PegasusPrincipalImpl) principal);
            return supplier.get();
        } finally {
            threadLocalPrincipals.set(previous);
        }
    }

    @Override
    public PegasusPrincipal principal(final AuthenticationToken token) {
        return new PegasusPrincipalImpl(token);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void restore(final PegasusPrincipal principal, final String password) {
        PegasusPrincipalImpl principalImpl = (PegasusPrincipalImpl) principal;
        synchronized (principalImpl) {
            if (principalImpl.getMemberContext() != null) {
                return;
            }
            try {
                threadLocalPrincipals.set(principalImpl);
                AuthenticationToken authenticationToken = authenticationTokenDAO
                        .retrieveById(principalImpl.getAuthenticationTokenId());
                Member member = memberDAO.retrieveByAuthenticationToken(authenticationToken);
                Vault vault = member.getDefaultVault();
                member = narrow(member, Member.class);
                vault = narrow(vaultService.openVault(vault.getId(), password), Vault.class);
                Organization organization = null;
                if (principalImpl.getOrganizationId() != null) {
                    organization = new Organization();
                    organization.setId(principalImpl.getOrganizationId());
                }
                restore(principalImpl, member, organization, vault);
            } catch (RuntimeException e) {
                // We need to bind early to support the latter restore operations, however if an error occurs, make sure
                // to unbind the broken context.
                principalImpl.setMemberContext(null);
                throw e;
            } finally {
                threadLocalPrincipals.remove();
            }
        }
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public boolean restore(final PegasusPrincipal principal, final byte[] secret) {
        PegasusPrincipalImpl principalImpl = (PegasusPrincipalImpl) principal;
        synchronized (principalImpl) {
            if (principalImpl.getMemberContext() != null) {
                // No restore necessary
                return false;
            }
            try {
                threadLocalPrincipals.set(principalImpl);
                ExportedPrincipal exportedPrincipal = principalImpl.getExportedPrincipal();
                if (exportedPrincipal == null) {
                    return false;
                }
                AuthenticatedPrincipal importedPrincipal = phalanxService.importPrincipal(exportedPrincipal, secret);
                AuthenticationToken authenticationToken = authenticationTokenDAO
                        .retrieveById(principalImpl.getAuthenticationTokenId());
                Member member = memberDAO.retrieveByAuthenticationToken(authenticationToken);
                Vault vault = member.getDefaultVault();
                member = narrow(member, Member.class);
                vault = narrow(vault, Vault.class);
                vault.setAuthenticatedPrincipal(importedPrincipal);
                Organization organization = null;
                if (principalImpl.getOrganizationId() != null) {
                    organization = new Organization();
                    organization.setId(principalImpl.getOrganizationId());
                }
                restore(principalImpl, member, organization, vault);
                return true;
            } catch (RuntimeException e) {
                // We need to bind early to support the latter restore operations, however if an error occurs, make sure
                // to unbind the broken context.
                principalImpl.setMemberContext(null);
                throw e;
            } finally {
                threadLocalPrincipals.remove();
            }
        }
    }

    private void restore(final PegasusPrincipalImpl principalImpl, final Member member, final Organization organization, final Vault vault) {
        MemberContextImpl memberContext = new MemberContextImpl(member);
        member.setDefaultVault(vault);
        memberContext.setActiveProfile(profileService.retrieveProfile(member));
        memberContext.retainVaultKey(vault);
        principalImpl.setMemberContext(memberContext);
        if (organization != null) {
            memberService.activateOrganization(organization);
        }
        applicationEventPublisher.publishEvent(new VaultOpenEvent(vault));
    }


    private void prepareRestore(final PegasusPrincipalImpl pegasusPrincipal, final Vault vault) {
        byte[] secret = randomCryptoService.generateBytes(24);
        ExportedPrincipal exportedPrincipal = phalanxService.exportPrincipal(vault.getAuthenticatedPrincipal(), secret);
        pegasusPrincipal.initRestore(secret, exportedPrincipal);
    }

    private static class PegasusPrincipalImpl extends PegasusPrincipal {

        /**
         * Serial UID
         */
        private static final long serialVersionUID = -2443068592543130002L;

        protected ExportedPrincipal exportedPrincipal;

        private UUID organizationId;



        PegasusPrincipalImpl(final AuthenticationToken authenticationToken) {
            super(authenticationToken);
        }

        void initRestore(final byte[] secret, final ExportedPrincipal principal) {
            this.restoreSecret = secret;
            this.exportedPrincipal = principal;
        }

        void setMemberContext(final MemberContext memberContext) {
            this.memberContext = memberContext;
        }

        UUID getOrganizationId() {
            return organizationId;
        }

        void setOrganizationId(final UUID organizationId) {
            this.organizationId = organizationId;
        }

        ExportedPrincipal getExportedPrincipal() {
            return exportedPrincipal;
        }
    }
}
