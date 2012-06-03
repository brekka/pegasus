/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import org.brekka.pegasus.core.dao.TransferDAO;
import org.brekka.pegasus.core.model.Bundle;
import org.brekka.pegasus.core.model.BundleFile;
import org.brekka.pegasus.core.model.Transfer;
import org.brekka.pegasus.core.services.BundleService;
import org.brekka.pegasus.core.services.EventService;
import org.brekka.phalanx.api.beans.IdentityCryptedData;
import org.brekka.phalanx.api.services.PhalanxService;
import org.brekka.phoenix.CryptoFactory;
import org.brekka.phoenix.CryptoFactoryRegistry;
import org.brekka.xml.pegasus.v1.model.BundleDocument;
import org.brekka.xml.pegasus.v1.model.BundleType;
import org.brekka.xml.pegasus.v1.model.FileType;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Service
@Transactional
public class BundleServiceImpl implements BundleService {
    @Autowired
    private BundleDAO bundleDAO;
    
    @Autowired
    private BundleFileDAO bundleFileDAO;
    
    @Autowired
    private PavewayService pavewayService;
    
    @Autowired
    private CryptoFactoryRegistry cryptoFactoryRegistry;
    
    @Autowired
    private ResourceStorageService resourceStorageService;
    
    @Autowired
    private ResourceCryptoService resourceCryptoService;
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private TransferDAO transferDAO;
    
    @Autowired
    private PhalanxService phalanxService;
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.BundleService#createBundle(java.lang.String, java.lang.String, 
     *      org.joda.time.DateTime, javax.crypto.SecretKey, int, java.util.List)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Bundle createBundle(String comment, String agreementText, String reference, DateTime expires, 
            int maxDownloads, SecretKey secretKey, int profile, List<FileBuilder> fileBuilders) {
        Bundle bundleModel = new Bundle();
        bundleModel.setId(UUID.randomUUID());
        bundleModel.setProfile(profile);
        bundleModel.setExpires(expires.toDate());
        
        BundleDocument doc = prepareBundleDocument(comment, agreementText, maxDownloads, fileBuilders);
        BundleType bundleType = doc.getBundle();
        bundleModel.setXml(bundleType);
        if (reference != null) {
            bundleType.setReference(reference);
        }
        
        encryptBundleDocument(doc, bundleModel, secretKey);
        bundleDAO.create(bundleModel);
        
        // Store the relationship between bundle and file (for de-allocator)
        allocateBundleFiles(bundleModel, bundleType);
        return bundleModel;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.BundleService#downloadCountForTransfer(org.brekka.pegasus.core.model.AnonymousTransfer)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public int downloadCountForTransfer(BundleFile bundleFile, Transfer transfer) {
        return eventService.fileDownloadCount(bundleFile, transfer);
    }
    
    

    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void decryptTransfer(Transfer transfer, byte[] secretKeyBytes) {
        try {
            Bundle bundle = transfer.getBundle();
            BundleType bundleType = decryptBundle(bundle, secretKeyBytes);
            bundle.setXml(bundleType);
            assignFileXml(bundle);
        } catch (XmlException | IOException e) {
            throw new PegasusException(PegasusErrorCode.PG200, e, 
                    "Failed to retrieve bundle XML for transfer '%s'", transfer.getId());
        }
        eventService.transferUnlocked(transfer);
    }


    /**
     * Perform the de-allocation
     * 
     * @param bundle
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void deallocateBundle(Bundle bundle) {
        List<BundleFile> fileList = bundleFileDAO.retrieveByBundle(bundle);
        for (BundleFile bundleFile : fileList) {
            // Bundle file id matches the crypted file id from paveway.
            deallocateBundleFile(bundleFile);
        }
        
        // Find the associated transfers and deallocate the keys in phalanx
        List<Transfer> transferList = transferDAO.retrieveByBundle(bundle);
        for (Transfer transfer : transferList) {
            UUID cryptedDataId = transfer.getCryptedDataId();
            phalanxService.deleteCryptedData(new IdentityCryptedData(cryptedDataId));
            transfer.setCryptedDataId(null);
            transferDAO.update(transfer);
        }
        
        // Clear the bundle XML
        resourceStorageService.remove(bundle.getId());
        
        // Mark as deleted, clear the IV
        bundle.setDeleted(new Date());
        bundle.setIv(null);
        bundleDAO.update(bundle);
    }
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void deallocateBundleFile(BundleFile bundleFile) {
        // Physically remove the file.
        if (bundleFile.getDeleted() == null) {
            // May have already been deleted.
            pavewayService.remove(bundleFile.getId());
            bundleFile.setDeleted(new Date());
            bundleFileDAO.update(bundleFile);
        }
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.BundleService#incrementDownloadCounter(org.brekka.pegasus.core.model.BundleFile)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void incrementDownloadCounter(BundleFile bundleFile) {
        FileType xml = bundleFile.getXml();
        int maxDownloads = Integer.MAX_VALUE;
        if (xml.isSetMaxDownloads()) {
            maxDownloads = xml.getMaxDownloads();
        }
        BundleFile managed = bundleFileDAO.retrieveById(bundleFile.getId());
        int downloadCount = managed.getDownloadCount();
        // Increment the downloads
        downloadCount++;
        managed.setDownloadCount(downloadCount);
        if (downloadCount == maxDownloads) {
            // Mark this file for deletion
            managed.setExpires(new Date());
        }
        bundleFileDAO.update(managed);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.BundleService#refreshBundle(org.brekka.pegasus.core.model.Bundle)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void refreshBundle(Bundle bundle) {
        bundleDAO.refresh(bundle);
        assignFileXml(bundle);
    }
    

    protected void assignFileXml(Bundle bundle) {
        BundleType bundleType = bundle.getXml();
        Map<UUID, BundleFile> files = bundle.getFiles();
        List<FileType> fileList = bundleType.getFileList();
        for (FileType fileType : fileList) {
            BundleFile bundleFile = files.get(UUID.fromString(fileType.getUUID()));
            bundleFile.setXml(fileType);
        }
    }
    
    protected BundleType decryptBundle(Bundle bundle, byte[] secretKeyBytes) throws XmlException, IOException {
        UUID bundleId = bundle.getId();
        ByteSequence byteSequence = resourceStorageService.retrieve(bundleId);
        IvParameterSpec iv = new IvParameterSpec(bundle.getIv());
        CryptoFactory cryptoFactory = cryptoFactoryRegistry.getFactory(bundle.getProfile());
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, cryptoFactory.getSymmetric().getKeyGenerator().getAlgorithm());
        
        try ( InputStream is = byteSequence.getInputStream(); ) {
            InputStream dis = resourceCryptoService.decryptor(bundle.getProfile(), Compression.GZIP, iv, secretKey, is);
            BundleDocument bundleDocument = BundleDocument.Factory.parse(dis);
            return bundleDocument.getBundle();
        }
    }
    
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
    protected BundleDocument prepareBundleDocument(String comment, String agreementText, 
            int maxDownloads, List<FileBuilder> fileBuilders) {
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
            fileXml.setMaxDownloads(maxDownloads);
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
}
