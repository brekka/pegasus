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
import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.Dispatch;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.PegasusTokenType;
import org.brekka.pegasus.core.model.Token;
import org.brekka.pegasus.core.model.XmlEntity;
import org.brekka.pegasus.core.services.AllocationService;
import org.brekka.pegasus.core.services.AnonymousTransferService;
import org.brekka.pegasus.core.services.EventService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.phalanx.api.PhalanxErrorCode;
import org.brekka.phalanx.api.PhalanxException;
import org.brekka.phalanx.api.services.PhalanxService;
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
    private PhalanxService phalanxService;
    
    @Autowired
    private RandomCryptoService randomCryptoService;
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private AllocationService allocationService;
    
    @Autowired
    private MemberService memberService;
    
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AnonymousTransferService#createBundle(java.util.List)
     */
    @Override
    @Transactional()
    public AnonymousTransfer createTransfer(Token token, DetailsType details, DateTime expires, Integer maxDownloads,
            Integer maxUnlockAttempts, UploadedFiles files, String code) {
        BundleType bundleType = completeFiles(maxDownloads, files);
        return createTransfer(token, details, expires, maxUnlockAttempts, null, bundleType, code);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AnonymousTransferService#createTransfer(org.brekka.xml.pegasus.v2.model.DetailsType, org.brekka.pegasus.core.model.Dispatch)
     */
    @Override
    @Transactional()
    public AnonymousTransfer createTransfer(Token token, DetailsType details, DateTime expires, Integer maxDownloads,
            Integer maxUnlockAttempts, Dispatch dispatch, String code) {
        BundleType dispatchBundle = copyDispatchBundle(dispatch, maxDownloads);
        return createTransfer(token, details, expires, maxUnlockAttempts, dispatch, dispatchBundle, code);
    }
    
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AnonymousTransferService#retrieveTransfer(java.lang.String)
     */
    @Override
    @Transactional(readOnly=true)
    public AnonymousTransfer retrieveUnlockedTransfer(String token) {
        AccessorContext accessorContext = AccessorContextImpl.getCurrent();
        AnonymousTransfer transfer = accessorContext.retrieve(token, AnonymousTransfer.class);
        if (transfer != null) {
            refreshAllocation(transfer);
        }
        // If the transfer is not unlocked, null will be returned.
        return transfer;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AnonymousTransferService#retrieveTransfer(java.lang.String)
     */
    @Override
    @Transactional(readOnly=true)
    public AnonymousTransfer retrieveTransfer(String token) {
        AnonymousTransfer transfer = retrieveByToken(token, AnonymousTransfer.class, false);
        return transfer;
    }
    
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AnonymousTransferService#agreementAccepted(java.lang.String)
     */
    @Override
    @Transactional()
    public void agreementAccepted(String token) {
        AnonymousTransfer transfer = retrieveByToken(token, AnonymousTransfer.class, true);
        eventService.agreementAccepted(transfer);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AnonymousTransferService#isAccepted(org.brekka.pegasus.core.model.Bundle)
     */
    @Override
    @Transactional(readOnly=true)
    public boolean isAccepted(AnonymousTransfer anonymousTransfer) {
        return eventService.isAccepted(anonymousTransfer);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AnonymousTransferService#unlock(java.lang.String, java.lang.String)
     */
    @Override
    @Transactional()
    public AnonymousTransfer unlock(String token, String code) {
        String codeClean = CODE_CLEAN_PATTERN.matcher(code).replaceAll("");
        AnonymousTransfer transfer = anonymousTransferDAO.retrieveByToken(token);
        try {
            decryptDocument(transfer, codeClean, true);
        } catch (PhalanxException e) {
            checkAttempts(e, transfer);
            throw e;
        }
        bindToContext(token, transfer);
        return transfer;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AnonymousTransferService#deleteTransfer(org.brekka.pegasus.core.model.AnonymousTransfer)
     */
    @Override
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void deleteTransfer(String token) {
        AnonymousTransfer transfer = retrieveByToken(token, AnonymousTransfer.class, true);
        transfer.setExpires(new Date());
        anonymousTransferDAO.update(transfer);
    }
    

    protected AnonymousTransfer createTransfer(Token token, DetailsType details, DateTime expires, Integer maxUnlockAttempts, Dispatch dispatch, 
            BundleType bundleType, String code) {
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
                SecureRandom random = randomCryptoService.getSecureRandom();
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
        XmlEntity<AllocationDocument> xmlEntity = xmlEntityService.persistEncryptedEntity(document, code, true);
        anonTransfer.setXml(xmlEntity);
        
        AuthenticatedMember<Member> current = memberService.getCurrent();
        if (current != null) {
            Actor activeActor = current.getActiveActor();
            anonTransfer.setActor(activeActor);
        }
        
        /*
         * Prepare the mapping between bundle and the url identifier that will be used to retrieve it by
         * the third party.
         */
        if (token == null) {
            token = tokenService.generateToken(PegasusTokenType.ANON);
        }
        anonTransfer.setToken(token);
        
        anonymousTransferDAO.create(anonTransfer);
        createAllocationFiles(anonTransfer);
        
        eventService.transferCreated(anonTransfer);
        return anonTransfer;
    }
    
    /**
     * Determine whether this transfer has more attempts left, expiring the transfer if it does not.
     * @param e
     * @param transfer
     * @return
     */
    private void checkAttempts(PhalanxException e, AnonymousTransfer transfer) {
        if (e.getErrorCode() == PhalanxErrorCode.CP302) {
            if (transfer.getMaxUnlockAttempts() != null) {
                int failedUnlockAttempts = eventService.retrieveFailedUnlockAttempts(transfer);
                if (failedUnlockAttempts >= transfer.getMaxUnlockAttempts()) {
                    allocationService.forceExpireAllocation(transfer);
                }
            }
        }
    }
}
