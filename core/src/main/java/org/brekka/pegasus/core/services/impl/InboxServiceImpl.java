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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.model.ListingCriteria;
import org.brekka.commons.persistence.support.EntityUtils;
import org.brekka.paveway.core.model.UploadedFiles;
import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.dao.DepositDAO;
import org.brekka.pegasus.core.dao.InboxDAO;
import org.brekka.pegasus.core.event.VaultDeleteEvent;
import org.brekka.pegasus.core.model.AccessorContext;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.AllocationDisposition;
import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.Deposit;
import org.brekka.pegasus.core.model.Dispatch;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.EMailAddress;
import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.PegasusTokenType;
import org.brekka.pegasus.core.model.Token;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.InboxService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.ProfileService;
import org.brekka.pegasus.core.services.TokenService;
import org.brekka.xml.pegasus.v2.model.AllocationDocument;
import org.brekka.xml.pegasus.v2.model.AllocationType;
import org.brekka.xml.pegasus.v2.model.BundleType;
import org.brekka.xml.pegasus.v2.model.DetailsType;
import org.brekka.xml.pegasus.v2.model.InboxType;
import org.brekka.xml.pegasus.v2.model.ProfileType;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inboxes accept deposits.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Service
public class InboxServiceImpl extends AllocationServiceSupport implements InboxService, ApplicationListener<ApplicationEvent>  {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private InboxDAO inboxDAO;

    @Autowired
    private DepositDAO depositDAO;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ProfileService profileService;


    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#createInbox(java.lang.String, org.brekka.pegasus.core.model.Vault)
     */
    @Override
    @Transactional()
    public Inbox createInbox(final String name, final String introduction, final String inboxToken, final KeySafe<? extends Actor> keySafe) {
        KeySafe<?> nKeySafe = EntityUtils.narrow(keySafe, KeySafe.class);
        Inbox inbox = new Inbox();
        inbox.setId(UUID.randomUUID());
        if (inboxToken != null) {
            Token token = this.tokenService.createToken(inboxToken, PegasusTokenType.INBOX);
            inbox.setToken(token);
        }
        inbox.setIntroduction(introduction);
        inbox.setKeySafe(nKeySafe);
        inbox.setName(name);
        inbox.setOwner(nKeySafe.getOwner());
        if (nKeySafe instanceof Division) {
            inbox.setDivision((Division<?>) nKeySafe);
        } else if (nKeySafe instanceof Vault) {
            AuthenticatedMember<Member> authenticatedMember = this.memberService.getCurrent(Member.class);
            if (authenticatedMember != null
                    && EntityUtils.identityEquals(authenticatedMember.getMember(), nKeySafe.getOwner())) {
                InboxType newXmlInbox = authenticatedMember.getProfile().addNewInbox();
                newXmlInbox.setUUID(inbox.getId().toString());
                newXmlInbox.setName(name);
                this.profileService.currentUserProfileUpdated();
            }
        }
        this.inboxDAO.create(inbox);
        return inbox;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.brekka.pegasus.core.services.InboxService#depositFiles(java.lang.String, java.lang.String,
     * java.util.List)
     */
    @Override
    @Transactional()
    public Deposit createDeposit(final Inbox inbox, final AllocationDisposition disposition, final DetailsType details, final DateTime expires,
            final UploadedFiles files) {
        BundleType bundleType = completeFiles(0, files);
        return createDeposit(inbox, disposition, details, expires, null, bundleType);
    }

    @Override
    @Transactional()
    public Deposit createDeposit(final Inbox inbox, final AllocationDisposition disposition, final DetailsType details, final DateTime expires,
            final Dispatch dispatch) {
        BundleType dispatchBundle = copyDispatchBundle(dispatch, null);
        return createDeposit(inbox, disposition, details, expires, dispatch, dispatchBundle);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#retrieveForToken(java.lang.String)
     */
    @Override
    @Transactional(readOnly=true)
    public Inbox retrieveForToken(final String inboxToken) {
        Token token = this.tokenService.retrieveByPath(inboxToken);
        Inbox inbox = this.inboxDAO.retrieveByToken(token);
        populateNames(Arrays.asList(inbox));
        return inbox;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#retrieveById(java.util.UUID)
     */
    @Override
    @Transactional(readOnly=true)
    public Inbox retrieveById(final UUID inboxId) {
        Inbox inbox = this.inboxDAO.retrieveById(inboxId);
        populateNames(Arrays.asList(inbox));
        return inbox;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#retrieveForVault(org.brekka.pegasus.core.model.Vault)
     */
    @Override
    @Transactional(readOnly=true)
    public List<Inbox> retrieveForKeySafe(final KeySafe<?> keySafe) {
        List<Inbox> inboxList = this.inboxDAO.retrieveForKeySafe(keySafe);
        populateNames(inboxList);
        return inboxList;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#retrieveForEMailAddress(org.brekka.pegasus.core.model.EMailAddress)
     */
    @Override
    @Transactional(readOnly=true)
    public Inbox retrieveForEMailAddress(final EMailAddress eMailAddress) {
        Inbox inbox = this.inboxDAO.retrieveForEMailAddress(eMailAddress);
        return inbox;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#retrieveForDivision(org.brekka.pegasus.core.model.Enlistment)
     */
    @Override
    @Transactional(readOnly=true)
    public List<Inbox> retrieveForDivision(final Division<?> division) {
        return this.inboxDAO.retrieveForDivision(division);
    }

    /* (non-Javadoc)
     *
     * @see org.brekka.pegasus.core.services.InboxService#retrieveDeposits(org.brekka.pegasus.core.model.Member,
     * org.brekka.pegasus.core.model.AllocationDisposition) */
    @Override
    public List<Deposit> retrieveDepositsByMember(final Member member,
            final AllocationDisposition allocationDisposition, final boolean personalOnly, final boolean includeExpired) {
        return this.depositDAO.retrieveDepositsForParticipant(member, allocationDisposition, personalOnly, includeExpired);
    }

    @Override
    public List<Deposit> retrieveDepositsByOwner(final Actor owner, final AllocationDisposition allocationDisposition,
            final boolean includePersonal, final boolean includeExpired) {
        return this.depositDAO.retrieveDepositsForCollectiveOwner(owner, allocationDisposition, includePersonal, includeExpired);
    }

    /* (non-Javadoc)
     *
     * @see org.brekka.pegasus.core.services.InboxService#unlock(org.brekka.pegasus.core.model.Deposit) */
    @Override
    @Transactional(readOnly=true)
    public Deposit retrieveDeposit(final UUID depositId, final boolean populateDispatches) {
        return populateDeposit(depositId, new DepositLookup() {
            @Override
            public Deposit retrieve(final UUID depositId) {
                return InboxServiceImpl.this.depositDAO.retrieveById(depositId);
            }
        }, populateDispatches);
    }


    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#retrieveDeposit(org.brekka.pegasus.core.model.Inbox, java.util.UUID)
     */
    @Override
    @Transactional(readOnly=true)
    public Deposit retrieveDeposit(final UUID depositId, final Inbox checkInInbox) {
        Deposit deposit = retrieveDeposit(depositId, false);
        if (deposit == null) {
            return null;
        }
        if (EntityUtils.identityEquals(deposit.getInbox(), checkInInbox)) {
            return deposit;
        }
        throw new PegasusException(PegasusErrorCode.PG745,
                "The deposit '%s' does not belong to inbox '%s'", depositId, checkInInbox.getId());
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#retrieveDeposit(java.util.UUID, org.brekka.pegasus.core.model.Member)
     */
    @Override
    @Transactional(readOnly=true)
    public Deposit retrieveDeposit(final UUID depositId, final Member memberCanAccess) {
        return populateDeposit(depositId, new DepositLookup() {
            @Override
            public Deposit retrieve(final UUID depositId) {
                return InboxServiceImpl.this.depositDAO.retrieveById(depositId, memberCanAccess);
            }
        }, false);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#retrieveForMember()
     */
    @Override
    @Transactional(readOnly=true)
    public List<Inbox> retrieveForMember() {
        AuthenticatedMember<Member> authenticatedMember = this.memberService.getCurrent(Member.class);
        List<Inbox> inboxList = this.inboxDAO.retrieveForMember(authenticatedMember.getMember());
        populateNames(inboxList);
        return inboxList;
    }


    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#retrieveDeposits(org.brekka.pegasus.core.model.Inbox)
     */
    @Override
    @Transactional(readOnly=true)
    public List<Deposit> retrieveDeposits(final Inbox inbox, final boolean releaseXml) {
        List<Deposit> depositList = this.depositDAO.retrieveByInbox(inbox);
        if (releaseXml) {
            for (Deposit deposit : depositList) {
                decryptDocument(deposit);
            }
        }
        return depositList;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#deleteDeposit(org.brekka.pegasus.core.model.Deposit)
     */
    @Override
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void deleteDeposit(final UUID depositId) {
        deleteDepositAfter(depositId, DateTime.now());
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#deleteDeposit(org.brekka.pegasus.core.model.Deposit)
     */
    @Override
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void deleteDepositAfter(final UUID depositId, final DateTime after) {
        Deposit deposit = this.depositDAO.retrieveById(depositId);
        deposit.setExpires(after.toDate());
        this.depositDAO.update(deposit);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.brekka.pegasus.core.services.InboxService#retrieveDepositListing(org.brekka.pegasus.core.model.Inbox,
     * org.joda.time.DateTime, org.joda.time.DateTime, boolean, org.brekka.commons.persistence.model.ListingCriteria)
     */
    @Override
    @Transactional(readOnly=true)
    public List<Deposit> retrieveDepositListing(final Inbox inbox, final DateTime from, final DateTime until, final boolean showExpired,
            final ListingCriteria listingCriteria, final boolean dispatchBased) {
        return retrieveDepositListing(inbox, from, until, showExpired, listingCriteria, dispatchBased, null);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#retrieveDepositListing(org.brekka.pegasus.core.model.Inbox, org.joda.time.DateTime, org.joda.time.DateTime, boolean, org.brekka.commons.persistence.model.ListingCriteria, boolean, java.util.List)
     */
    @Override
    public List<Deposit> retrieveDepositListing(final Inbox inbox, final DateTime from, final DateTime until, final boolean showExpired,
            final ListingCriteria listingCriteria, final boolean dispatchBased, final List<? extends Actor> sentByActors) {
        List<Deposit> depositList = this.depositDAO.retrieveListing(inbox, defaultMin(from), defaultMax(until), showExpired,
                listingCriteria, dispatchBased, sentByActors);
        if (dispatchBased) {
            List<Dispatch> dispatchList = new ArrayList<>();
            for (Deposit deposit : depositList) {
                Dispatch derivedFrom = deposit.getDerivedFrom();
                dispatchList.add(derivedFrom);
            }
            this.xmlEntityService.releaseAll(dispatchList, AllocationDocument.class);
        } else {
            this.xmlEntityService.releaseAll(depositList, AllocationDocument.class);
        }
        return depositList;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.brekka.pegasus.core.services.InboxService#retrieveDepositListingRowCount(org.brekka.pegasus.core.model.Inbox,
     * org.joda.time.DateTime, org.joda.time.DateTime, boolean)
     */
    @Override
    @Transactional(readOnly=true)
    public int retrieveDepositListingRowCount(final Inbox inbox, final DateTime from, final DateTime until, final boolean showExpired,
            final boolean dispatchBased) {
        return this.depositDAO.retrieveListingRowCount(inbox, defaultMin(from), defaultMax(until), showExpired,
                dispatchBased);
    }

    @Override
    public void onApplicationEvent(final ApplicationEvent event) {
        if (event instanceof VaultDeleteEvent) {
            VaultDeleteEvent vaultDeleteEvent = (VaultDeleteEvent) event;
            this.inboxDAO.deleteWithKeySafe(vaultDeleteEvent.getVault());
        }
    }


    protected Deposit createDeposit(Inbox inbox, final AllocationDisposition disposition, final DetailsType details,
            final DateTime expires, final Dispatch dispatch, final BundleType newBundleType) {
        Deposit deposit = new Deposit();
        deposit.setDerivedFrom(dispatch);

        // Bring the inbox under management
        inbox = this.inboxDAO.retrieveById(inbox.getId());
        KeySafe<?> keySafe = inbox.getKeySafe();
        deposit.setExpires(expires == null ? null : expires.toDate());

        AllocationType allocationType = prepareAllocationType(newBundleType, details);

        // Encrypt the document
        prepareDocument(deposit, allocationType, keySafe);

        deposit.setInbox(inbox);
        deposit.setKeySafe(keySafe);
        deposit.setDisposition(disposition);

        Token token = this.tokenService.generateToken(PegasusTokenType.DEPOSIT);
        deposit.setToken(token);

        AuthenticatedMember<Member> current = this.memberService.getCurrent();
        if (current != null) {
            Actor activeActor = current.getActiveActor();
            deposit.setActor(activeActor);
        }

        createAllocationFiles(deposit);
        this.depositDAO.create(deposit);

        return deposit;
    }

    protected void populateNames(final Collection<Inbox> inboxes) {
        AuthenticatedMember<Member> authenticatedMember = this.memberService.getCurrent(Member.class);
        if (authenticatedMember == null) {
            return;
        }
        ProfileType profile = authenticatedMember.getProfile();
        if (profile != null) {
            for (Inbox inbox : inboxes) {
                if (inbox != null) {
                    for (int i = 0; i < profile.sizeOfInboxArray(); i++) {
                        InboxType inboxXml = profile.getInboxArray(i);
                        if (inboxXml.getUUID().equals(inbox.getId().toString())) {
                            String name = inboxXml.getName();
                            inbox.setName(name);
                            break;
                        }
                    }
                }
            }
        }
    }

    protected Deposit populateDeposit(final UUID depositId, final DepositLookup depositLookup, final boolean populateDispatches) {
        AccessorContext current = AccessorContextImpl.getCurrent();
        Deposit deposit = current.retrieve(depositId, Deposit.class);
        if (deposit == null) {
            // Need to extract the metadata
            deposit = depositLookup.retrieve(depositId);
            if (deposit.getDeleted() != null) {
                // Deposit has been deleted, unable to decrypt
                return deposit;
            }
            if (populateDispatches) {
                decryptDocument(deposit.getDerivedFrom());
            } else {
                decryptDocument(deposit);
            }
        } else {
            // Already unlocked, just refresh
            refreshAllocation(deposit);
        }
        return deposit;
    }

    /**
     * @param from
     * @return
     */
    private static DateTime defaultMin(DateTime from) {
        if (from == null) {
            from = new DateTime(0);
        }
        return from;
    }

    /**
     * @param until
     * @return
     */
    private static DateTime defaultMax(DateTime until) {
        if (until == null) {
            until = new DateTime().plusYears(100);
        }
        return until;
    }

    interface DepositLookup {
        Deposit retrieve(UUID depositId);
    }
}
