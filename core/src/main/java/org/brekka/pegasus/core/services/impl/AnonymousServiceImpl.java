/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.xmlbeans.XmlException;
import org.brekka.paveway.core.model.AllocatedFile;
import org.brekka.paveway.core.model.ByteSequence;
import org.brekka.paveway.core.model.Compression;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.services.PavewayService;
import org.brekka.paveway.core.services.ResourceCryptoService;
import org.brekka.paveway.core.services.ResourceEncryptor;
import org.brekka.paveway.core.services.ResourceStorageService;
import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.dao.AnonymousTransferDAO;
import org.brekka.pegasus.core.dao.BundleDAO;
import org.brekka.pegasus.core.model.AnonymousTransfer;
import org.brekka.pegasus.core.model.Bundle;
import org.brekka.pegasus.core.model.Slug;
import org.brekka.pegasus.core.model.TransferKey;
import org.brekka.pegasus.core.services.AnonymousService;
import org.brekka.pegasus.core.services.EventService;
import org.brekka.pegasus.core.services.SlugService;
import org.brekka.phalanx.api.beans.IdentityCryptedData;
import org.brekka.phalanx.api.model.CryptedData;
import org.brekka.phalanx.api.services.PhalanxService;
import org.brekka.phoenix.CryptoFactory;
import org.brekka.phoenix.CryptoFactoryRegistry;
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
public class AnonymousServiceImpl implements AnonymousService {
    
    @Autowired
    private BundleDAO bundleDAO;
    @Autowired
    private AnonymousTransferDAO anonymousTransferDAO;
    
    @Autowired
    private ResourceStorageService resourceStorageService;
    
    @Autowired
    private ResourceCryptoService resourceCryptoService;
    
    @Autowired
    private CryptoFactoryRegistry cryptoFactoryRegistry;
    
    @Autowired
    private PhalanxService phalanxService;
    
    @Autowired
    private PavewayService pavewayService;
    
    @Autowired
    private SlugService slugService;
    
    @Autowired
    private EventService eventService;
    

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AnonymousService#createBundle(java.util.List)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public TransferKey createBundle(String comment, List<FileBuilder> fileBuilders) {
        Bundle bundleModel = new Bundle();
        bundleModel.setId(UUID.randomUUID());
        
        /*
         * Prepare the XML based structure that will contain the details for this bundle,
         * while will be subsequently encrypted.
         */
        BundleDocument doc = BundleDocument.Factory.newInstance();
        BundleType bundleXml = doc.addNewBundle();
        for (FileBuilder fileBuilder : fileBuilders) {
            AllocatedFile allocatedFile = pavewayService.complete(fileBuilder);
            FileType fileXml = bundleXml.addNewFile();
            fileXml.setName(allocatedFile.getFileName());
            fileXml.setMimeType(allocatedFile.getMimeType());
            fileXml.setUUID(allocatedFile.getCryptedFile().getId().toString());
            fileXml.setKey(allocatedFile.getSecretKey().getEncoded());
            fileXml.setLength(allocatedFile.getCryptedFile().getOriginalLength());
        }
        bundleXml.setComment(comment);
        
        // Store the bundle XML blob
        ;
        
        /*
         * Encrypt the bundle XML 
         */
        CryptoFactory defaultCryptoFactory = cryptoFactoryRegistry.getDefault();
        SecretKey secretKey = defaultCryptoFactory.getSymmetric().getKeyGenerator().generateKey();
        ResourceEncryptor encryptor = resourceCryptoService.encryptor(secretKey, Compression.GZIP);
        ByteSequence byteSequence = resourceStorageService.allocate(bundleModel.getId());
        OutputStream os = byteSequence.getOutputStream();
        try ( OutputStream eos = encryptor.encrypt(os) ) {
            doc.save(eos);
        } catch (IOException e) {
            throw new PegasusException(PegasusErrorCode.PG200, e, 
                    "Failed to store bundle XML");
        }
        
        // Allocate a code
        final String code = RandomStringUtils.random(8, 0, 0, false, true, null, defaultCryptoFactory.getSecureRandom());
        
        /*
         * Use phalanx to store the secret key for the bundle XML, encrypted with the code.
         */
        CryptedData pbeEncryptedData = phalanxService.pbeEncrypt(secretKey.getEncoded(), code);
        bundleModel.setCryptedDataId(pbeEncryptedData.getId());
        
        bundleModel.setIv(encryptor.getIV().getIV());
        bundleModel.setProfile(defaultCryptoFactory.getProfileId());
        bundleDAO.create(bundleModel);
        
        
        
        /*
         * Prepare the mapping between bundle and the url identifier that will be used to retrieve it by
         * the third party.
         */
        final Slug slug = slugService.allocateAnonymous();
        AnonymousTransfer anonTransfer = new AnonymousTransfer();
        anonTransfer.setBundle(bundleModel);
        anonTransfer.setSlug(slug);
        
        eventService.bundleCreated(bundleModel);
        anonymousTransferDAO.create(anonTransfer);
        return new TransferKey() {
            @Override
            public String getSlug() {
                return slug.getPath();
            }
            @Override
            public String getCode() {
                return code;
            }
        };
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AnonymousService#unlock(java.lang.String, java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public BundleType unlock(String slug, String code, Date agreementAccepted) {
        
        AnonymousTransfer transfer = anonymousTransferDAO.retrieveBySlug(slug);
        
        Bundle bundle = transfer.getBundle();
        UUID bundleId = bundle.getId();
        
        byte[] secretKeyBytes = phalanxService.pbeDecrypt(new IdentityCryptedData(bundle.getCryptedDataId()), code);
        CryptoFactory cryptoFactory = cryptoFactoryRegistry.getFactory(bundle.getProfile());
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, cryptoFactory.getSymmetric().getKeyGenerator().getAlgorithm());
        IvParameterSpec iv = new IvParameterSpec(bundle.getIv());
        
        ByteSequence byteSequence = resourceStorageService.retrieve(bundleId);
        
        try ( InputStream is = byteSequence.getInputStream(); ) {
            InputStream dis = resourceCryptoService.decryptor(bundle.getProfile(), Compression.GZIP, iv, secretKey, is);
            
            eventService.bundleUnlocked(bundle, agreementAccepted);
            
            BundleDocument bundleDocument = BundleDocument.Factory.parse(dis);
            return bundleDocument.getBundle();
        } catch (XmlException | IOException e) {
            throw new PegasusException(PegasusErrorCode.PG200, e, 
                    "Failed to retrieve bundle XML for slug '%s'" , slug);
        }
    }

}
