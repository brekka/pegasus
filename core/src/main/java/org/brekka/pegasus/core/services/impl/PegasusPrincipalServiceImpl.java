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
import java.util.concurrent.TimeUnit;

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

import com.google.common.base.Stopwatch;

/**
 *
 */
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
            throw new PegasusException(PegasusErrorCode.PG902, "No pegasus principal could be found in the current security context");
        }
        return pegasusPrincipal;
    }

    @Override
    @Transactional()
    public void logout(final PegasusPrincipal pegasusPrincipal) {
        MemberContextImpl memberContext = (MemberContextImpl) pegasusPrincipal.getMemberContext();
        Stopwatch sw = Stopwatch.createStarted();
        if (memberContext != null) {
            List<AuthenticatedPrincipal> authenticatedPrincipals = memberContext.clearVaults();
            for (AuthenticatedPrincipal authenticatedPrincipal : authenticatedPrincipals) {
                this.phalanxService.logout(authenticatedPrincipal);
            }
        }
        ((PegasusPrincipalImpl) pegasusPrincipal).setMemberContext(null);
        if (log.isInfoEnabled()) {
            log.info(String.format("Pegasus logout for '%s' took %d ms",
                    pegasusPrincipal.getName(), sw.elapsed(TimeUnit.MILLISECONDS)));
        }
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void loginAndBind(final PegasusPrincipalAware principalSource, final String password, final Organization organization,
            final boolean restoreRequired) {
        Stopwatch sw = Stopwatch.createStarted();
        PegasusPrincipalImpl pegasusPrincipal = (PegasusPrincipalImpl) principalSource.getPegasusPrincipal();
        AuthenticationToken authenticationToken = authenticationTokenDAO.retrieveById(pegasusPrincipal.getAuthenticationTokenId());
        Member member = memberDAO.retrieveByAuthenticationToken(authenticationToken);
        if (member.getStatus() != ActorStatus.ACTIVE) {
            throw new DisabledException(String.format("Member '%s' account is disabled", member.getId()));
        }
        Vault vault = member.getDefaultVault();
        member = narrow(member, Member.class);
        MemberContextImpl memberContext = new MemberContextImpl(member);
        pegasusPrincipal.setMemberContext(memberContext);
        Profile activeProfile = profileService.retrieveProfile(member);
        memberContext.setActiveProfile(activeProfile != null ? activeProfile : new Profile());
        threadLocalPrincipals.set(pegasusPrincipal);

        vault = narrow(vaultService.openVault(vault.getId(), password), Vault.class);
        member.setDefaultVault(vault);
        if (organization != null) {
            memberService.activateOrganization(organization);
        }
        if (restoreRequired) {
            prepareRestore(pegasusPrincipal, vault);
        }
        pegasusPrincipal.setOrganizationId(organization != null ? organization.getId() : null);
        if (log.isInfoEnabled()) {
            log.info(String.format("Pegasus login for '%s' with organization '%s', took %d ms",
                    pegasusPrincipal.getName(), organization != null ? organization.getName() : null, sw.elapsed(TimeUnit.MILLISECONDS)));
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
    public PegasusPrincipal principal(final AuthenticationToken token) {
        return new PegasusPrincipalImpl(token);
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void restore(final PegasusPrincipal principal, final String password) {
        PegasusPrincipalImpl principalImpl = (PegasusPrincipalImpl) principal;
        try {
            threadLocalPrincipals.set(principalImpl);

            AuthenticationToken authenticationToken = authenticationTokenDAO.retrieveById(principalImpl.getAuthenticationTokenId());
            Member member = memberDAO.retrieveByAuthenticationToken(authenticationToken);
            Vault vault = member.getDefaultVault();
            member = narrow(member, Member.class);
            MemberContextImpl memberContext = new MemberContextImpl(member);
            memberContext.setActiveProfile(profileService.retrieveProfile(member));
            vault = narrow(vaultService.openVault(vault.getId(), password), Vault.class);
            member.setDefaultVault(vault);
            memberContext.retainVaultKey(vault);
            restore(principalImpl, memberContext);
            applicationEventPublisher.publishEvent(new VaultOpenEvent(vault));

        } finally {
            threadLocalPrincipals.remove();
        }
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public boolean restore(final PegasusPrincipal principal, final byte[] secret) {
        PegasusPrincipalImpl principalImpl = (PegasusPrincipalImpl) principal;
        try {
            threadLocalPrincipals.set(principalImpl);
            ExportedPrincipal exportedPrincipal = principalImpl.getExportedPrincipal();
            if (exportedPrincipal == null) {
                return false;
            }
            AuthenticatedPrincipal importedPrincipal = phalanxService.importPrincipal(exportedPrincipal, secret);
            AuthenticationToken authenticationToken = authenticationTokenDAO.retrieveById(principalImpl.getAuthenticationTokenId());
            Member member = memberDAO.retrieveByAuthenticationToken(authenticationToken);
            Vault vault = member.getDefaultVault();
            member = narrow(member, Member.class);
            MemberContextImpl memberContext = new MemberContextImpl(member);
            memberContext.setActiveProfile(profileService.retrieveProfile(member));
            vault = narrow(vault, Vault.class);
            member.setDefaultVault(vault);
            vault.setAuthenticatedPrincipal(importedPrincipal);
            memberContext.retainVaultKey(vault);
            restore(principalImpl, memberContext);
            applicationEventPublisher.publishEvent(new VaultOpenEvent(vault));
            return true;
        } finally {
            threadLocalPrincipals.remove();
        }
    }

    private void restore(final PegasusPrincipalImpl principalImpl, final MemberContextImpl memberContext) {
        principalImpl.setMemberContext(memberContext);
        if (principalImpl.getOrganizationId() != null) {
            Organization organization = new Organization();
            organization.setId(principalImpl.getOrganizationId());
            memberService.activateOrganization(organization);
        }
    }


    /**
     * @param pegasusPrincipal
     * @param memberContext
     */
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
