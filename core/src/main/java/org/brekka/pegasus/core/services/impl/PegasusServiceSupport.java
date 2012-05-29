/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
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
import org.brekka.pegasus.core.dao.BundleDAO;
import org.brekka.pegasus.core.dao.BundleFileDAO;
import org.brekka.pegasus.core.model.Bundle;
import org.brekka.pegasus.core.model.BundleFile;
import org.brekka.pegasus.core.model.Transfer;
import org.brekka.pegasus.core.services.EventService;
import org.brekka.pegasus.core.services.KeySafeService;
import org.brekka.phalanx.api.services.PhalanxService;
import org.brekka.phoenix.CryptoFactory;
import org.brekka.phoenix.CryptoFactoryRegistry;
import org.brekka.xml.pegasus.v1.model.BundleDocument;
import org.brekka.xml.pegasus.v1.model.BundleType;
import org.brekka.xml.pegasus.v1.model.FileType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
abstract class PegasusServiceSupport {
    
    @Autowired
    protected BundleDAO bundleDAO;
    
    @Autowired
    protected BundleFileDAO bundleFileDAO;
    
    
    @Autowired
    protected PhalanxService phalanxService;
    
    @Autowired
    protected PavewayService pavewayService;
    
    @Autowired
    protected CryptoFactoryRegistry cryptoFactoryRegistry;
    
    @Autowired
    protected ResourceStorageService resourceStorageService;
    
    @Autowired
    protected ResourceCryptoService resourceCryptoService;
    
    @Autowired
    protected EventService eventService;
    
    @Autowired
    protected KeySafeService keySafeService;
    
    /**
     * Allocate {@link BundleFile} instances for each file in the builder list.
     * @param bundle
     * @param fileBuilders
     */
    protected void allocateBundleFiles(Bundle bundle, BundleType bundleType) {
        List<FileType> fileList = bundleType.getFileList();
        for (FileType fileType : fileList) {
            UUID id = UUID.fromString(fileType.getUUID());
            BundleFile bf = new BundleFile(id, bundle);
            bundleFileDAO.create(bf);
        }
    }
    
    /**
     * Prepare the XML based structure that will contain the details for this bundle,
     * while will be subsequently encrypted.
     * @param comment
     * @param fileBuilders
     * @return
     */
    protected BundleDocument prepareBundleDocument(String comment, String agreementText, List<FileBuilder> fileBuilders) {
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
        if (StringUtils.isNotEmpty(agreementText)) {
            bundleXml.setAgreement(agreementText);
        }
        return doc;
    }
    
    protected void encryptBundleDocument(BundleDocument encryptBundleDoc, Bundle toBundleModel, SecretKey secretKey) {
        ResourceEncryptor encryptor = resourceCryptoService.encryptor(secretKey, Compression.GZIP);
        ByteSequence byteSequence = resourceStorageService.allocate(toBundleModel.getId());
        OutputStream os = byteSequence.getOutputStream();
        try ( OutputStream eos = encryptor.encrypt(os) ) {
            encryptBundleDoc.save(eos);
        } catch (IOException e) {
            throw new PegasusException(PegasusErrorCode.PG200, e, 
                    "Failed to store bundle XML");
        }
        toBundleModel.setIv(encryptor.getIV().getIV());
    }
    


    protected BundleType decryptTransfer(Transfer transfer, byte[] secretKeyBytes) throws XmlException, IOException {
        Bundle bundle = transfer.getBundle();
        UUID bundleId = bundle.getId();
        ByteSequence byteSequence = resourceStorageService.retrieve(bundleId);
        IvParameterSpec iv = new IvParameterSpec(bundle.getIv());
        CryptoFactory cryptoFactory = cryptoFactoryRegistry.getFactory(bundle.getProfile());
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, cryptoFactory.getSymmetric().getKeyGenerator().getAlgorithm());
        
        try ( InputStream is = byteSequence.getInputStream(); ) {
            InputStream dis = resourceCryptoService.decryptor(bundle.getProfile(), Compression.GZIP, iv, secretKey, is);
            
            eventService.transferUnlocked(transfer);
            
            BundleDocument bundleDocument = BundleDocument.Factory.parse(dis);
            return bundleDocument.getBundle();
        }
    }
}
