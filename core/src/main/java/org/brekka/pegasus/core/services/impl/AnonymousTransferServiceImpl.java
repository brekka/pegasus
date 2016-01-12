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

import java.security.SecureRandom;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomStringUtils;
import org.brekka.paveway.core.model.UploadedFiles;
import org.brekka.pegasus.core.dao.AnonymousTransferDAO;
import org.brekka.pegasus.core.model.AccessorContext;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.AnonymousTransfer;
import org.brekka.pegasus.core.model.Dispatch;
import org.brekka.pegasus.core.model.MemberContext;
import org.brekka.pegasus.core.model.PegasusTokenType;
import org.brekka.pegasus.core.model.Token;
import org.brekka.pegasus.core.model.XmlEntity;
import org.brekka.pegasus.core.services.AllocationService;
import org.brekka.pegasus.core.services.AnonymousTransferService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.phalanx.api.PhalanxErrorCode;
import org.brekka.phalanx.api.PhalanxException;
import org.brekka.phoenix.api.services.RandomCryptoService;
import org.brekka.xml.pegasus.v2.model.AllocationDocument;
import org.brekka.xml.pegasus.v2.model.AllocationType;
import org.brekka.xml.pegasus.v2.model.BundleType;
import org.brekka.xml.pegasus.v2.model.DetailsType;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manipulate anonymous transfers.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Service
@Transactional
public class AnonymousTransferServiceImpl extends AllocationServiceSupport implements AnonymousTransferService {

    /**
     * Removes non-word characters from the code pattern.
     */
    private static final Pattern CODE_CLEAN_PATTERN = Pattern.compile("[^\\w]+", Pattern.UNICODE_CHARACTER_CLASS);

    @Autowired
    private AnonymousTransferDAO anonymousTransferDAO;

    @Autowired
    private RandomCryptoService randomCryptoService;

    @Autowired
    private AllocationService allocationService;

    @Autowired
    private MemberService memberService;


    @Override
    @Transactional()
    public AnonymousTransfer createTransfer(final Token token, final DetailsType details, final DateTime expires, final Integer maxDownloads,
            final Integer maxUnlockAttempts, final UploadedFiles files, final String code) {
        BundleType bundleType = completeFiles(maxDownloads, files);
        return createTransfer(token, details, expires, maxUnlockAttempts, null, bundleType, code);
    }

    @Override
    @Transactional()
    public AnonymousTransfer createTransfer(final Token token, final DetailsType details, final DateTime expires, final Integer maxDownloads,
            final Integer maxUnlockAttempts, final Dispatch dispatch, final String code) {
        BundleType dispatchBundle = copyDispatchBundle(dispatch, maxDownloads);
        return createTransfer(token, details, expires, maxUnlockAttempts, dispatch, dispatchBundle, code);
    }

    @Override
    @Transactional(readOnly=true)
    public AnonymousTransfer retrieveUnlockedTransfer(final String token) {
        AccessorContext accessorContext = AccessorContextImpl.getCurrent();
        AnonymousTransfer transfer = accessorContext.retrieve(token, AnonymousTransfer.class);
        if (transfer != null) {
            refreshAllocation(transfer);
        }
        // If the transfer is not unlocked, null will be returned.
        return transfer;
    }

    @Override
    @Transactional(readOnly=true)
    public AnonymousTransfer retrieveTransfer(final String token) {
        AnonymousTransfer transfer = retrieveByToken(token, AnonymousTransfer.class, false);
        return transfer;
    }

    @Override
    @Transactional()
    public void agreementAccepted(final String token) {
        AnonymousTransfer transfer = retrieveByToken(token, AnonymousTransfer.class, true);
        this.eventService.agreementAccepted(transfer);
    }

    @Override
    @Transactional(readOnly=true)
    public boolean isAccepted(final AnonymousTransfer anonymousTransfer) {
        return this.eventService.isAccepted(anonymousTransfer);
    }

    @Override
    @Transactional()
    public AnonymousTransfer unlock(final String token, final String code, final boolean external) {
        String codeClean = CODE_CLEAN_PATTERN.matcher(code).replaceAll("");
        AnonymousTransfer transfer = this.anonymousTransferDAO.retrieveByToken(token);
        try {
            decryptDocument(transfer, codeClean, external);
        } catch (PhalanxException e) {
            checkAttempts(e, transfer);
            throw e;
        }
        bindToContext(token, transfer);
        return transfer;
    }

    @Override
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void deleteTransfer(final String token) {
        AnonymousTransfer transfer = retrieveByToken(token, AnonymousTransfer.class, true);
        transfer.setExpires(new Date());
        this.anonymousTransferDAO.update(transfer);
    }


    protected AnonymousTransfer createTransfer(Token token, final DetailsType details, final DateTime expires, final Integer maxUnlockAttempts, final Dispatch dispatch,
            final BundleType bundleType, String code) {
        AnonymousTransfer anonTransfer = new AnonymousTransfer();
        anonTransfer.setDerivedFrom(dispatch);
        anonTransfer.setExpires(expires.toDate());
        anonTransfer.setMaxUnlockAttempts(maxUnlockAttempts);

        AllocationType allocationType = prepareAllocationType(bundleType, details);

        // Allocate a code
        String prettyCode;
        if (code == null) {
            StringBuilder codeBuilder = new StringBuilder();
            StringBuilder prettyCodeBuilder = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                if (i > 0) {
                    prettyCodeBuilder.append(" ");
                }
                SecureRandom random = this.randomCryptoService.getSecureRandom();
                String codePart = RandomStringUtils.random(2, 0, 0, false, true, null, random);
                prettyCodeBuilder.append(codePart);
                codeBuilder.append(codePart);
            }
            code = codeBuilder.toString();
            prettyCode = prettyCodeBuilder.toString();
        } else {
            prettyCode = code;
            code = CODE_CLEAN_PATTERN.matcher(prettyCode).replaceAll("");
        }
        anonTransfer.setCode(prettyCode);

        AllocationDocument document = AllocationDocument.Factory.newInstance();
        document.setAllocation(allocationType);
        XmlEntity<AllocationDocument> xmlEntity = this.xmlEntityService.persistEncryptedEntity(document, code, true);
        anonTransfer.setXml(xmlEntity);

        MemberContext current = this.memberService.getCurrent();
        if (current != null) {
            Actor activeActor = current.getActiveActor();
            anonTransfer.setActor(activeActor);
        }

        /*
         * Prepare the mapping between bundle and the url identifier that will be used to retrieve it by
         * the third party.
         */
        if (token == null) {
            token = this.tokenService.generateToken(PegasusTokenType.ANON);
        }
        anonTransfer.setToken(token);

        this.anonymousTransferDAO.create(anonTransfer);
        createAllocationFiles(anonTransfer);

        this.eventService.transferCreated(anonTransfer);
        return anonTransfer;
    }

    /**
     * Determine whether this transfer has more attempts left, expiring the transfer if it does not.
     * @param e
     * @param transfer
     * @return
     */
    private void checkAttempts(final PhalanxException e, final AnonymousTransfer transfer) {
        if (e.getErrorCode() == PhalanxErrorCode.CP302) {
            if (transfer.getMaxUnlockAttempts() != null) {
                int failedUnlockAttempts = this.eventService.retrieveFailedUnlockAttempts(transfer);
                if (failedUnlockAttempts >= transfer.getMaxUnlockAttempts()) {
                    this.allocationService.forceExpireAllocation(transfer);
                }
            }
        }
    }
}
