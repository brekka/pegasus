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

package org.brekka.pegasus.core.services;

import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.model.ListingCriteria;
import org.brekka.paveway.core.model.UploadedFiles;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.AllocationDisposition;
import org.brekka.pegasus.core.model.Deposit;
import org.brekka.pegasus.core.model.Dispatch;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.EMailAddress;
import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Participant;
import org.brekka.xml.pegasus.v2.model.DetailsType;
import org.joda.time.DateTime;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface InboxService {

    /**
     * Create a new inbox for the current user, using the specified vault.
     *
     * @param token
     * @param keySafe
     * @return
     */
    Inbox createInbox(String name, String introduction, String inboxToken, KeySafe<?> keySafe);

    /**
     * Create a deposit in the specified inbox.
     *
     * @param inboxToken
     * @param comment
     * @param fileBuilders
     * @return
     */
    Deposit createDeposit(Inbox inbox, AllocationDisposition disposition, DetailsType details, DateTime expires,
            UploadedFiles files);

    /**
     *
     * @param inbox
     * @param details
     * @param bundleType
     * @param dispatch
     * @return
     */
    Deposit createDeposit(Inbox inbox, AllocationDisposition disposition, DetailsType details, DateTime expires,
            Dispatch dispatch);

    /**
     * Retrieve the inboxes owned by this member.
     *
     * @return
     */
    List<Inbox> retrieveForMember();

    /**
     * @param inboxToken
     * @return
     */
    Inbox retrieveForToken(String inboxToken);

    /**
     * E-Mail address
     *
     * @param eMailAddress
     * @return
     */
    Inbox retrieveForEMailAddress(EMailAddress eMailAddress);

    /**
     * Retrieve all deposits from the specified inbox.
     *
     * @param inbox
     * @return
     */
    List<Deposit> retrieveDeposits(Inbox inbox, boolean releaseXml);

    /**
     * Retrieve all deposits of a given type assigned to the specified member via any collective the user is a member
     * of.
     *
     * @param member
     * @param allocationDisposition
     * @return
     */
    List<Deposit> retrieveDepositsByMember(Member member, AllocationDisposition allocationDisposition,
            boolean personalOnly, boolean includeExpired);

    List<Deposit> retrieveDepositsByOwner(Actor owner, AllocationDisposition allocationDisposition, boolean includePersonal, boolean includeExpired);

    /**
     * Retrieve the specified deposit which will contain the file decryption key metadata.
     *
     * @param deposit
     * @return
     */
    Deposit retrieveDeposit(UUID depositId, boolean populateDispatches);

    /**
     * Retrieve the specified deposit and verify that it belongs to the specified inbox.
     *
     * @param depositId
     * @param checkInInbox
     * @return
     */
    Deposit retrieveDeposit(UUID depositId, Inbox checkInInbox);

    /**
     * Retrieve the deposit with the specified id, and make sure that the specified member has access (via
     * {@link Participant}).
     *
     * @param depositId
     * @param canAccess
     * @return
     */
    Deposit retrieveDeposit(UUID depositId, Member memberCanAccess);

    /**
     * @param keySafe
     * @return
     */
    List<Inbox> retrieveForKeySafe(KeySafe<?> keySafe);

    /**
     * @param loopDivision
     * @return
     */
    List<Inbox> retrieveForDivision(Division<?> division);

    /**
     * @param actionDeposit
     */
    void deleteDeposit(UUID depositId);

    /**
     * @param depositId
     * @param after
     */
    void deleteDepositAfter(UUID depositId, DateTime after);

    int retrieveDepositListingRowCount(Inbox inbox, DateTime from, DateTime until, boolean showExpired,
            boolean dispatchBased);

    List<Deposit> retrieveDepositListing(Inbox inbox, DateTime from, DateTime until, boolean showExpired,
            ListingCriteria listingCriteria, boolean populateDispatches);

    List<Deposit> retrieveDepositListing(Inbox inbox, DateTime from, DateTime until, boolean showExpired,
            ListingCriteria listingCriteria, boolean populateDispatches, List<? extends Actor> sentByActors);

    /**
     * @param fromString
     * @return
     */
    Inbox retrieveById(UUID inboxId);

}
