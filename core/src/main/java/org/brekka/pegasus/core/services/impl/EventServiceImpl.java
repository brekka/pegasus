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

import org.brekka.pegasus.core.dao.AgreementAcceptedEventDAO;
import org.brekka.pegasus.core.dao.BundleCreatedEventDAO;
import org.brekka.pegasus.core.dao.BundleUnlockEventDAO;
import org.brekka.pegasus.core.dao.FileDownloadEventDAO;
import org.brekka.pegasus.core.model.AgreementAcceptedEvent;
import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.model.AllocationFile;
import org.brekka.pegasus.core.model.FileDownloadEvent;
import org.brekka.pegasus.core.model.MemberContext;
import org.brekka.pegasus.core.model.RemoteUserEvent;
import org.brekka.pegasus.core.model.Transfer;
import org.brekka.pegasus.core.model.TransferCreatedEvent;
import org.brekka.pegasus.core.model.TransferUnlockEvent;
import org.brekka.pegasus.core.model.XmlEntity;
import org.brekka.pegasus.core.security.WebAuthenticationDetails;
import org.brekka.pegasus.core.services.EventService;
import org.brekka.pegasus.core.services.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Captures events produced within Pegasus
 *
 * TODO needs to be more generic. Perhaps hooked into the Spring event mechanism and using {@link XmlEntity} to store
 * event specific metadata.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Service
@Transactional
public class EventServiceImpl implements EventService {

    @Autowired
    private FileDownloadEventDAO fileDownloadEventDAO;

    @Autowired
    private BundleUnlockEventDAO bundleUnlockEventDAO;

    @Autowired
    private BundleCreatedEventDAO bundleCreatedEventDAO;

    @Autowired
    private AgreementAcceptedEventDAO agreementAcceptedEventDAO;

    @Autowired
    private MemberService memberService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void transferUnlock(final Transfer transfer, final boolean success) {
        TransferUnlockEvent event = new TransferUnlockEvent();
        event.setTransfer(transfer);
        event.setSuccess(success);
        populate(event);
        bundleUnlockEventDAO.create(event);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FileDownloadEvent beginFileDownloadEvent(final AllocationFile bundleFile) {
        FileDownloadEvent event = new FileDownloadEvent();
        event.setTransferFile(bundleFile);
        populate(event);
        fileDownloadEventDAO.create(event);
        return event;
    }

    @Override
    @Transactional()
    public int fileDownloadCount(final AllocationFile bundleFile, final Transfer transfer) {
        return fileDownloadEventDAO.fileDownloadCount(bundleFile, transfer);
    }

    @Override
    @Transactional()
    public boolean isAccepted(final Transfer transfer) {
        return retrieveAgreement(transfer) != null;
    }

    @Override
    @Transactional()
    public AgreementAcceptedEvent retrieveAgreement(final Transfer transfer) {
        return agreementAcceptedEventDAO.retrieveByTransfer(transfer);
    }

    @Override
    @Transactional()
    public void transferCreated(final Transfer transfer) {
        TransferCreatedEvent event = new TransferCreatedEvent();
        event.setTransfer(transfer);
        populate(event);
        bundleCreatedEventDAO.create(event);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeEvent(final FileDownloadEvent event) {
        event.setCompleted(new Date());
        populate(event);
        fileDownloadEventDAO.update(event);
    }

    @Override
    @Transactional()
    public void agreementAccepted(final Transfer transfer) {
        AgreementAcceptedEvent event = new AgreementAcceptedEvent();
        event.setTransfer(transfer);
        populate(event);
        agreementAcceptedEventDAO.create(event);
    }

    @Override
    @Transactional(readOnly=true)
    public int retrieveFailedUnlockAttempts(final Transfer transfer) {
        return bundleUnlockEventDAO.retrieveFailedUnlockAttempts(transfer);
    }

    @Override
    @Transactional(readOnly=true)
    public List<TransferUnlockEvent> retrieveUnlockAttempts(final Transfer transfer) {
        return bundleUnlockEventDAO.retrieveAttempts(transfer);
    }

    @Override
    @Transactional(readOnly=true)
    public List<FileDownloadEvent> retrieveFileDownloads(final Allocation allocation) {
        return fileDownloadEventDAO.retrieveFileDownloads(allocation);
    }

    protected void populate(final RemoteUserEvent remoteUserEvent) {
        remoteUserEvent.setInitiated(new Date());
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        Object details = authentication.getDetails();
        if (details instanceof WebAuthenticationDetails) {
            WebAuthenticationDetails wad = (WebAuthenticationDetails) details;
            remoteUserEvent.setOnBehalfOfAddress(wad.getOnBehalfOfAddress());
            remoteUserEvent.setRemoteAddress(wad.getRemoteAddress());
            remoteUserEvent.setUserAgent(wad.getUserAgent());
        } else {
            throw new IllegalStateException(String.format(
                    "No web authentication details found in authentication %s, principal: %s",
                    authentication.getClass().getName(), authentication.getPrincipal()));
        }

        MemberContext current = memberService.getCurrent();
        if (current != null) {
            remoteUserEvent.setMember(current.getMember());
        }
    }
}
