/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.security.SecureRandom;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomStringUtils;
import org.brekka.paveway.core.model.CompletableFile;
import org.brekka.pegasus.core.dao.AnonymousTransferDAO;
import org.brekka.pegasus.core.model.AccessorContext;
import org.brekka.pegasus.core.model.AnonymousTransfer;
import org.brekka.pegasus.core.model.Dispatch;
import org.brekka.pegasus.core.model.Token;
import org.brekka.pegasus.core.model.TokenType;
import org.brekka.pegasus.core.services.AnonymousService;
import org.brekka.pegasus.core.services.TokenService;
import org.brekka.phalanx.api.beans.IdentityCryptedData;
import org.brekka.phalanx.api.model.CryptedData;
import org.brekka.phalanx.api.services.PhalanxService;
import org.brekka.phoenix.api.SecretKey;
import org.brekka.phoenix.api.services.RandomCryptoService;
import org.brekka.xml.pegasus.v2.model.AllocationDocument;
import org.brekka.xml.pegasus.v2.model.AllocationType;
import org.brekka.xml.pegasus.v2.model.BundleType;
import org.brekka.xml.pegasus.v2.model.DetailsType;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Andrew Taylor
 *
 */
@Service
@Transactional
public class AnonymousServiceImpl extends AllocationServiceSupport implements AnonymousService {
    
    /**
     * 
     */
    private static final Pattern CODE_CLEAN_PATTERN = Pattern.compile("[^\\w]+", Pattern.UNICODE_CHARACTER_CLASS);

    @Autowired
    private AnonymousTransferDAO anonymousTransferDAO;
    
    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private PhalanxService phalanxService;
    
    @Autowired
    private RandomCryptoService randomCryptoService;
    
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AnonymousService#createBundle(java.util.List)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public AnonymousTransfer createTransfer(DetailsType details, DateTime expires, Integer maxDownloads, Integer maxUnlockAttempts,
            List<CompletableFile> files, String code) {
        BundleType bundleType = completeFiles(maxDownloads, files);
        return createTransfer(details, expires, maxUnlockAttempts, null, bundleType, code);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AnonymousService#createTransfer(org.brekka.xml.pegasus.v2.model.DetailsType, org.brekka.pegasus.core.model.Dispatch)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public AnonymousTransfer createTransfer(DetailsType details, DateTime expires, Integer maxDownloads, Integer maxUnlockAttempts, 
            Dispatch dispatch, String code) {
        BundleType dispatchBundle = copyDispatchBundle(dispatch, maxDownloads);
        return createTransfer(details, expires, maxUnlockAttempts, dispatch, dispatchBundle, code);
    }
    
    protected AnonymousTransfer createTransfer(DetailsType details, DateTime expires, Integer maxUnlockAttempts, Dispatch dispatch, 
            BundleType bundleType, String code) {
        AnonymousTransfer anonTransfer = new AnonymousTransfer();
        anonTransfer.setDerivedFrom(dispatch);
        anonTransfer.setExpires(expires.toDate());
        anonTransfer.setMaxUnlockAttempts(maxUnlockAttempts);
        
        AllocationDocument document = prepareDocument(bundleType);
        AllocationType allocationType = document.getAllocation();
        allocationType.setDetails(details);
        
        // Encrypt the document
        encryptDocument(anonTransfer, document);
        SecretKey secretKey = anonTransfer.getSecretKey();
        anonTransfer.setSecretKey(null);
        
        
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
        
        /*
         * Use phalanx to store the secret key for the bundle XML, encrypted with the code.
         */
        CryptedData pbeEncryptedData = phalanxService.pbeEncrypt(secretKey.getEncoded(), code);
        anonTransfer.setCryptedDataId(pbeEncryptedData.getId());
        
        /*
         * Prepare the mapping between bundle and the url identifier that will be used to retrieve it by
         * the third party.
         */
        Token token = tokenService.generateToken(TokenType.ANON);
        anonTransfer.setToken(token);
        
        anonymousTransferDAO.create(anonTransfer);
        createAllocationFiles(anonTransfer);
        
        eventService.transferCreated(anonTransfer);
        return anonTransfer;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AnonymousService#retrieveTransfer(java.lang.String)
     */
    @Override
    public AnonymousTransfer retrieveTransfer(String token) {
        AccessorContext accessorContext = AccessorContextImpl.getCurrent();
        AnonymousTransfer transfer = accessorContext.retrieve(token, AnonymousTransfer.class);
        if (transfer != null) {
            refreshAllocation(transfer);
        }
        // If the transfer is not unlocked, null will be returned.
        return transfer;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AnonymousService#agreementAccepted(java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void agreementAccepted(String token) {
        AnonymousTransfer transfer = anonymousTransferDAO.retrieveByToken(token);
        eventService.agreementAccepted(transfer);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AnonymousService#isAccepted(org.brekka.pegasus.core.model.Bundle)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public boolean isAccepted(AnonymousTransfer anonymousTransfer) {
        return eventService.isAccepted(anonymousTransfer);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AnonymousService#unlock(java.lang.String, java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public AnonymousTransfer unlock(String token, String code) {
        String codeClean = CODE_CLEAN_PATTERN.matcher(code).replaceAll("");
        AnonymousTransfer transfer = anonymousTransferDAO.retrieveByToken(token);
        byte[] secretKeyBytes = phalanxService.pbeDecrypt(new IdentityCryptedData(transfer.getCryptedDataId()), codeClean);
        decryptDocument(transfer, secretKeyBytes);
        
        bindToContext(token, transfer);
        return transfer;
    }
}
