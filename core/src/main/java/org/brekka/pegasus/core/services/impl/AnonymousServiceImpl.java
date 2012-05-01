/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.xmlbeans.XmlException;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.dao.AnonymousTransferDAO;
import org.brekka.pegasus.core.model.AnonymousTransfer;
import org.brekka.pegasus.core.model.Bundle;
import org.brekka.pegasus.core.model.Token;
import org.brekka.pegasus.core.model.TransferKey;
import org.brekka.pegasus.core.services.AnonymousService;
import org.brekka.pegasus.core.services.TokenService;
import org.brekka.phalanx.api.beans.IdentityCryptedData;
import org.brekka.phalanx.api.model.CryptedData;
import org.brekka.phoenix.CryptoFactory;
import org.brekka.xml.pegasus.v1.model.BundleDocument;
import org.brekka.xml.pegasus.v1.model.BundleType;
import org.brekka.xml.pegasus.v1.model.FileType;
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
public class AnonymousServiceImpl extends PegasusServiceSupport implements AnonymousService {
    
    @Autowired
    private AnonymousTransferDAO anonymousTransferDAO;
    
    @Autowired
    private TokenService tokenService;
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AnonymousService#createBundle(java.util.List)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public TransferKey createBundle(String comment, List<FileBuilder> fileBuilders) {
        Bundle bundleModel = new Bundle();
        bundleModel.setId(UUID.randomUUID());
        
        BundleDocument doc = prepareBundleDocument(comment, fileBuilders);
        
        // Fetch the default crypto factory, generate a new secret key
        CryptoFactory defaultCryptoFactory = cryptoFactoryRegistry.getDefault();
        SecretKey secretKey = defaultCryptoFactory.getSymmetric().getKeyGenerator().generateKey();
        bundleModel.setProfile(defaultCryptoFactory.getProfileId());
        
        encryptBundleDocument(doc, bundleModel, secretKey);
        
        // Allocate a code
        String code = RandomStringUtils.random(8, 0, 0, false, true, null, defaultCryptoFactory.getSecureRandom());
        
        /*
         * Use phalanx to store the secret key for the bundle XML, encrypted with the code.
         */
        CryptedData pbeEncryptedData = phalanxService.pbeEncrypt(secretKey.getEncoded(), code);
        bundleModel.setCryptedDataId(pbeEncryptedData.getId());
        bundleDAO.create(bundleModel);
        
        /*
         * Prepare the mapping between bundle and the url identifier that will be used to retrieve it by
         * the third party.
         */
        Token token = tokenService.allocateAnonymous();
        AnonymousTransfer anonTransfer = new AnonymousTransfer();
        anonTransfer.setBundle(bundleModel);
        anonTransfer.setToken(token);
        
        eventService.bundleCreated(bundleModel);
        anonymousTransferDAO.create(anonTransfer);
        
        List<FileType> fileList = doc.getBundle().getFileList();
        String fileName = null;
        if (fileList.size() == 1) {
            fileName = fileList.get(0).getName();
        }
        return new AnonymousTransferKeyImpl(token.getPath(), code, fileName);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AnonymousService#unlock(java.lang.String, java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public BundleType unlock(String token, String code, Date agreementAccepted) {
        
        AnonymousTransfer transfer = anonymousTransferDAO.retrieveByToken(token);
        
        Bundle bundle = transfer.getBundle();
        byte[] secretKeyBytes = phalanxService.pbeDecrypt(new IdentityCryptedData(bundle.getCryptedDataId()), code);
        
        try {
            return decryptBundle(agreementAccepted, bundle, secretKeyBytes);
        } catch (XmlException | IOException e) {
            throw new PegasusException(PegasusErrorCode.PG200, e, 
                    "Failed to retrieve bundle XML for token '%s'" , token);
        }
    }
}
