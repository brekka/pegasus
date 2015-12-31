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

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.brekka.commons.persistence.support.EntityUtils;
import org.brekka.paveway.core.model.UploadedFiles;
import org.brekka.pegasus.core.dao.DispatchDAO;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.model.AllocationDisposition;
import org.brekka.pegasus.core.model.Dispatch;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.EMailAddress;
import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.MemberContext;
import org.brekka.pegasus.core.model.PegasusTokenType;
import org.brekka.pegasus.core.model.Token;
import org.brekka.pegasus.core.services.AnonymousTransferService;
import org.brekka.pegasus.core.services.DispatchService;
import org.brekka.pegasus.core.services.EMailAddressService;
import org.brekka.pegasus.core.services.InboxService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.xml.pegasus.v2.model.AllocationType;
import org.brekka.xml.pegasus.v2.model.BundleType;
import org.brekka.xml.pegasus.v2.model.DetailsType;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * A dispatch is an allocation created by a registered user with the goal of assigning it to another user.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Service
@Transactional
public class DispatchServiceImpl extends AllocationServiceSupport implements DispatchService {

    @Autowired
    private MemberService memberService;

    @Autowired
    private EMailAddressService eMailAddressService;

    @Autowired
    private InboxService inboxService;

    @Autowired
    private AnonymousTransferService anonymousService;

    @Autowired
    private DispatchDAO dispatchDAO;

    @Override
    @Transactional()
    public Dispatch createDispatch(final KeySafe<?> keySafe, final AllocationDisposition disposition, final DetailsType details,
            final DateTime expires, final Integer maxDownloads, final UploadedFiles files) {
        KeySafe<?> nKeySafe = EntityUtils.narrow(keySafe, KeySafe.class);
        Dispatch dispatch = new Dispatch();
        MemberContext memberContext = memberService.getCurrent();
        Actor activeActor = null;
        if (memberContext != null) {
            activeActor = memberContext.getActiveActor();
        }

        BundleType bundleType = completeFiles(0, files);

        // Copy the allocation to
        AllocationType allocationType = prepareAllocationType(bundleType, details);
        prepareDocument(dispatch, allocationType, nKeySafe);

        if (nKeySafe instanceof Division) {
            dispatch.setDivision((Division<?>) nKeySafe);
        }
        dispatch.setKeySafe(nKeySafe);
        dispatch.setActor(activeActor);
        dispatch.setExpires(expires != null ? expires.toDate() : null);
        dispatch.setDisposition(disposition);

        Token token = this.tokenService.generateToken(PegasusTokenType.DISPATCH);
        dispatch.setToken(token);

        this.dispatchDAO.create(dispatch);
        createAllocationFiles(dispatch);
        return dispatch;
    }

    @Override
    @Transactional()
    public Allocation createDispatchAndAllocate(final String recipientEMail, final Division<?> division, final KeySafe<?> keySafe,
            final DetailsType details, final DateTime dispatchExpires, final DateTime allocationExpires, final int maxDownloads, final UploadedFiles files) {
        Dispatch dispatch = createDispatch(keySafe, null, details, dispatchExpires, null, files);

        Inbox inbox = null;
        if (StringUtils.isNotBlank(recipientEMail)) {
            EMailAddress address = this.eMailAddressService.retrieveByAddress(recipientEMail);
            if (address != null) {
                // Known to the system.
                inbox = this.inboxService.retrieveForEMailAddress(address);
            }
        }
        Allocation allocation;
        if (inbox != null) {
            allocation = this.inboxService.createDeposit(inbox, null, details, allocationExpires, dispatch);
        } else {
            allocation = this.anonymousService.createTransfer(null, details, allocationExpires, maxDownloads, null, dispatch, null);
        }
        return allocation;
    }


    @Override
    @Transactional(readOnly=true)
    public List<Dispatch> retrieveCurrentForInterval(final KeySafe<?> keySafe, final DateTime from, final DateTime until) {
        MemberContext memberContext = memberService.getCurrent();
        Actor activeActor = memberContext.getActiveActor();
        return this.dispatchDAO.retrieveForInterval(keySafe, activeActor, from.toDate(), until.toDate());
    }

    @Override
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void delete(final UUID dispatchId) {
        Dispatch dispatch = this.dispatchDAO.retrieveById(dispatchId);
        delete(dispatch);
    }

    @Override
    @Transactional
    public void delete(final Dispatch dispatch) {
        dispatch.setExpires(new Date());
        this.dispatchDAO.update(dispatch);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Dispatch> find(final KeySafe<?> keySafe, final AllocationDisposition disposition) {
        return this.dispatchDAO.find(keySafe, disposition);
    }
}
