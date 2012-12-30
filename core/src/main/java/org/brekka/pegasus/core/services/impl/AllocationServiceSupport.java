/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.apache.xmlbeans.XmlException;
import org.brekka.paveway.core.dao.CryptedFileDAO;
import org.brekka.paveway.core.model.ByteSequence;
import org.brekka.paveway.core.model.CompletableUploadedFile;
import org.brekka.paveway.core.model.Compression;
import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.paveway.core.model.ResourceEncryptor;
import org.brekka.paveway.core.model.UploadedFiles;
import org.brekka.paveway.core.services.PavewayService;
import org.brekka.paveway.core.services.ResourceCryptoService;
import org.brekka.paveway.core.services.ResourceStorageService;
import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.dao.AllocationDAO;
import org.brekka.pegasus.core.dao.AllocationFileDAO;
import org.brekka.pegasus.core.model.AccessorContext;
import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.model.AllocationFile;
import org.brekka.pegasus.core.model.Dispatch;
import org.brekka.pegasus.core.model.Transfer;
import org.brekka.pegasus.core.services.EventService;
import org.brekka.pegasus.core.services.KeySafeService;
import org.brekka.phalanx.api.services.PhalanxService;
import org.brekka.phoenix.api.CryptoProfile;
import org.brekka.phoenix.api.SecretKey;
import org.brekka.phoenix.api.StreamCryptor;
import org.brekka.phoenix.api.SymmetricCryptoSpec;
import org.brekka.phoenix.api.services.CryptoProfileService;
import org.brekka.phoenix.api.services.SymmetricCryptoService;
import org.brekka.xml.pegasus.v2.model.AllocationDocument;
import org.brekka.xml.pegasus.v2.model.AllocationType;
import org.brekka.xml.pegasus.v2.model.BundleType;
import org.brekka.xml.pegasus.v2.model.FileType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
class AllocationServiceSupport {

    @Autowired
    protected PhalanxService phalanxService;
    
    @Autowired
    protected PavewayService pavewayService;
    
    @Autowired
    protected ResourceStorageService resourceStorageService;
    
    @Autowired
    protected SymmetricCryptoService symmetricCryptoService;
    
    @Autowired
    protected ResourceCryptoService resourceCryptoService;
    
    @Autowired
    protected CryptoProfileService cryptoProfileService;
    
    @Autowired
    protected EventService eventService;
    
    @Autowired
    protected KeySafeService keySafeService;

    @Autowired
    private AllocationDAO allocationDAO;
    
    @Autowired
    private AllocationFileDAO allocationFileDAO;
    
    @Autowired
    private CryptedFileDAO cryptedFileDAO;
    
    

    protected BundleType completeFiles(int maxDownloads, UploadedFiles files) {
        if (files == null) {
            return null;
        }
        BundleType bundleType = BundleType.Factory.newInstance();
        List<CompletableUploadedFile> ready = files.uploadComplete();
        for (CompletableUploadedFile file : ready) {
            CryptedFile cryptedFile = pavewayService.complete(file);
            
            FileType fileXml = bundleType.addNewFile();
            fileXml.setName(cryptedFile.getFileName());
            fileXml.setMimeType(cryptedFile.getMimeType());
            fileXml.setUUID(cryptedFile.getId().toString());
            fileXml.setKey(cryptedFile.getSecretKey().getEncoded());
            fileXml.setLength(cryptedFile.getOriginalLength());
            if (maxDownloads > 0) {
                fileXml.setMaxDownloads(maxDownloads);
            }
        }
        return bundleType;
    }
    
    
    /**
     * @param maxDownloads
     * @param bundleType
     * @return
     */
    protected BundleType copyBundle(Integer maxDownloads, BundleType bundleType) {
        BundleType b = BundleType.Factory.newInstance();
        List<FileType> fileList = bundleType.getFileList();
        for (FileType file : fileList) {
            FileType f = b.addNewFile();
            f.setKey(file.getKey());
            f.setLength(file.getLength());
            f.setMimeType(file.getMimeType());
            f.setName(file.getName());
            f.setUUID(file.getUUID());
            if (maxDownloads != null) {
                f.setMaxDownloads(maxDownloads);
            }
        }
        return b;
    }
    /**
     * @param dispatch
     * @return
     */
    protected BundleType copyDispatchBundle(Dispatch dispatch, Integer maxDownloads) {
        AllocationType dispatchXml = dispatch.getXml();
        if (dispatchXml == null) {
            // Need to decrypt
            byte[] secretKeyBytes = keySafeService.release(dispatch.getCryptedDataId(), dispatch.getKeySafe());
            decryptDocument(dispatch, secretKeyBytes);
            dispatchXml = dispatch.getXml();
        }
        BundleType dispatchBundle = copyBundle(maxDownloads, dispatchXml.getBundle());
        return dispatchBundle;
    }
    
    /**
     * Prepare the XML based structure that will contain the details for this bundle,
     * while will be subsequently encrypted.
     * @param comment
     * @param fileBuilders
     * @return
     */
    protected AllocationDocument prepareDocument(BundleType bundleType) {
        AllocationDocument doc = AllocationDocument.Factory.newInstance();
        AllocationType allocationType = doc.addNewAllocation();
        if (bundleType != null) {
            allocationType.setBundle(bundleType);
        }
        return doc;
    }
    
    protected void decryptDocument(Allocation allocation, byte[] secretKeyBytes) {
        if (allocation.getXml() != null) {
            // Already decrypted
            return;
        }
        try {
            AllocationType allocationType = decrypt(allocation, secretKeyBytes);
            allocation.setXml(allocationType);
            assignFileXml(allocation);
        } catch (XmlException | IOException e) {
            throw new PegasusException(PegasusErrorCode.PG200, e, 
                    "Failed to retrieve XML for allocation '%s'", allocation.getId());
        }
        if (allocation instanceof Transfer) {
            eventService.transferUnlocked((Transfer) allocation);
        }
    }
    
    protected void createAllocationFiles(Allocation allocation) {
        List<FileType> fileList = allocation.getXml().getBundle().getFileList();
        for (FileType fileType : fileList) {
            AllocationFile allocationFile = new AllocationFile();
            allocationFile.setAllocation(allocation);
            CryptedFile cryptedFile = cryptedFileDAO.retrieveById(UUID.fromString(fileType.getUUID()));
            allocationFile.setCryptedFile(cryptedFile);
            allocationFile.setXml(fileType);
            allocationFileDAO.create(allocationFile);
        }
    }
    
    
    protected void assignFileXml(Allocation allocation) {
        AllocationType allocationType = allocation.getXml();
        List<AllocationFile> files = allocation.getFiles();
        List<FileType> fileList = allocationType.getBundle().getFileList();
        // Use nested loops as there should never be that many files.
        for (FileType fileType : fileList) {
            UUID cryptedFileID = UUID.fromString(fileType.getUUID());
            for (AllocationFile allocationFile : files) {
                UUID id = allocationFile.getCryptedFile().getId();
                if (id.equals(cryptedFileID)) {
                    allocationFile.setXml(fileType);
                    break; // Break out of this loop
                }
            }
        }
    }
    
    
    private AllocationType decrypt(Allocation allocation, byte[] secretKeyBytes) throws XmlException, IOException {
        UUID allocationId = allocation.getId();
        ByteSequence byteSequence = resourceStorageService.retrieve(allocationId);
        CryptoProfile cryptoProfile = cryptoProfileService.retrieveProfile(allocation.getProfile());
        SecretKey secretKey = symmetricCryptoService.toSecretKey(secretKeyBytes, cryptoProfile);
        allocation.setSecretKey(secretKey);
        try ( InputStream is = byteSequence.getInputStream(); ) {
            StreamCryptor<InputStream, SymmetricCryptoSpec> decryptor = resourceCryptoService.decryptor(allocation, Compression.GZIP);
            InputStream dis = decryptor.getStream(is);
            AllocationDocument allocationDocument = AllocationDocument.Factory.parse(dis);
            allocation.setXml(allocation.getXml());
            return allocationDocument.getAllocation();
        }
    }
    
    
    protected void encryptDocument(Allocation allocation, AllocationDocument allocationDoc) {
        allocation.setId(UUID.randomUUID());
        // Fetch the default crypto factory, generate a new secret key
        CryptoProfile cryptoProfile = cryptoProfileService.retrieveDefault();
        SecretKey secretKey = symmetricCryptoService.createSecretKey(cryptoProfile);
        
        ResourceEncryptor encryptor = resourceCryptoService.encryptor(secretKey, Compression.GZIP);
        ByteSequence byteSequence = resourceStorageService.allocate(allocation.getId());
        OutputStream os = byteSequence.getOutputStream();
        try ( OutputStream eos = encryptor.encrypt(os) ) {
            allocationDoc.save(eos);
        } catch (IOException e) {
            throw new PegasusException(PegasusErrorCode.PG200, e, 
                    "Failed to store bundle XML");
        }
        allocation.setProfile(cryptoProfile.getNumber());
        allocation.setSecretKey(secretKey);
        allocation.setIv(encryptor.getSpec().getIV());
        allocation.setXml(allocationDoc.getAllocation());
    }
    
    protected void bindToContext(Allocation allocation) {
        bindToContext(allocation.getId(), allocation);
    }
    
    protected void bindToContext(Serializable key, Allocation allocation) {
        AccessorContext accessorContext = AccessorContextImpl.getCurrent();
        accessorContext.retain(key, allocation);
        List<AllocationFile> files = allocation.getFiles();
        for (AllocationFile allocationFile : files) {
            accessorContext.retain(allocationFile.getCryptedFile().getId(), allocationFile);
        }
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AllocationService#refreshAllocation(org.brekka.pegasus.core.model.AnonymousTransfer)
     */
    protected void refreshAllocation(Allocation allocation) {
        allocationDAO.refresh(allocation);
        assignFileXml(allocation);
    }
}