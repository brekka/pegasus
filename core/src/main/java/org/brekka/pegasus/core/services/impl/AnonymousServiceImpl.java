/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.List;

import javax.crypto.SecretKey;

import org.apache.commons.lang3.RandomStringUtils;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.pegasus.core.dao.AnonymousTransferDAO;
import org.brekka.pegasus.core.model.AnonymousTransfer;
import org.brekka.pegasus.core.model.Bundle;
import org.brekka.pegasus.core.model.Token;
import org.brekka.pegasus.core.model.TokenType;
import org.brekka.pegasus.core.services.AnonymousService;
import org.brekka.pegasus.core.services.BundleService;
import org.brekka.pegasus.core.services.EventService;
import org.brekka.pegasus.core.services.TokenService;
import org.brekka.phalanx.api.beans.IdentityCryptedData;
import org.brekka.phalanx.api.model.CryptedData;
import org.brekka.phalanx.api.services.PhalanxService;
import org.brekka.phoenix.CryptoFactory;
import org.brekka.phoenix.CryptoFactoryRegistry;
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
public class AnonymousServiceImpl implements AnonymousService {
    
    @Autowired
    private AnonymousTransferDAO anonymousTransferDAO;
    
    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private BundleService bundleService;
    
    @Autowired
    private PhalanxService phalanxService;
    
    @Autowired
    private CryptoFactoryRegistry cryptoFactoryRegistry;
    
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AnonymousService#createBundle(java.util.List)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public AnonymousTransfer createTransfer(String comment, String agreementText, int maxDownloads, List<FileBuilder> fileBuilders) {
        // Fetch the default crypto factory, generate a new secret key
        CryptoFactory defaultCryptoFactory = cryptoFactoryRegistry.getDefault();
        SecretKey secretKey = defaultCryptoFactory.getSymmetric().getKeyGenerator().generateKey();
        
        // TODO Expiry, currently fixed at 12 hours, should be configured.
        DateTime now = new DateTime();
        DateTime expires = now.plusHours(12);
        
        Bundle bundleModel = bundleService.createBundle(comment, agreementText, null, expires, 
                maxDownloads, secretKey, defaultCryptoFactory.getProfileId(), fileBuilders);
        
        // Allocate a code
        StringBuilder codeBuilder = new StringBuilder();
        StringBuilder prettyCodeBuilder = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            if (i > 0) {
                prettyCodeBuilder.append(" ");
            }
            String codePart = RandomStringUtils.random(2, 0, 0, false, true, null, defaultCryptoFactory.getSecureRandom());
            prettyCodeBuilder.append(codePart);
            codeBuilder.append(codePart);
        }
        
        /*
         * Use phalanx to store the secret key for the bundle XML, encrypted with the code.
         */
        CryptedData pbeEncryptedData = phalanxService.pbeEncrypt(secretKey.getEncoded(), codeBuilder.toString());
        
        /*
         * Prepare the mapping between bundle and the url identifier that will be used to retrieve it by
         * the third party.
         */
        Token token = tokenService.generateToken(TokenType.ANON);
        AnonymousTransfer anonTransfer = new AnonymousTransfer();
        anonTransfer.setBundle(bundleModel);
        anonTransfer.setToken(token);
        anonTransfer.setCryptedDataId(pbeEncryptedData.getId());
        anonTransfer.setSecretKey(secretKey);
        
        anonymousTransferDAO.create(anonTransfer);
        eventService.bundleCreated(bundleModel);
        return anonTransfer;
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
        String codeClean = code.replaceAll("[^0-9]+", "");
        AnonymousTransfer transfer = anonymousTransferDAO.retrieveByToken(token);
        byte[] secretKeyBytes = phalanxService.pbeDecrypt(new IdentityCryptedData(transfer.getCryptedDataId()), codeClean);
        bundleService.decryptTransfer(transfer, secretKeyBytes);
        return transfer;
    }
}
