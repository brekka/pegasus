/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.XmlException;
import org.brekka.paveway.core.model.ByteSequence;
import org.brekka.paveway.core.model.Compression;
import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.services.PavewayService;
import org.brekka.paveway.core.services.ResourceCryptoService;
import org.brekka.paveway.core.services.ResourceEncryptor;
import org.brekka.paveway.core.services.ResourceStorageService;
import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.dao.AllocationDAO;
import org.brekka.pegasus.core.dao.AllocationFileDAO;
import org.brekka.pegasus.core.model.AccessorContext;
import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.model.AllocationFile;
import org.brekka.pegasus.core.model.Transfer;
import org.brekka.pegasus.core.services.EventService;
import org.brekka.phalanx.api.services.PhalanxService;
import org.brekka.phoenix.api.CryptoProfile;
import org.brekka.phoenix.api.SecretKey;
import org.brekka.phoenix.api.StreamCryptor;
import org.brekka.phoenix.api.SymmetricCryptoSpec;
import org.brekka.phoenix.api.services.CryptoProfileService;
import org.brekka.phoenix.api.services.SymmetricCryptoService;
import org.brekka.xml.pegasus.v1.model.AllocationDocument;
import org.brekka.xml.pegasus.v1.model.AllocationType;
import org.brekka.xml.pegasus.v1.model.BundleType;
import org.brekka.xml.pegasus.v1.model.FileType;
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
    private AllocationDAO allocationDAO;
    
    @Autowired
    private AllocationFileDAO allocationFileDAO;
    

    protected BundleType completeFiles(int maxDownloads, List<FileBuilder> fileBuilders) {
        BundleType bundleType = BundleType.Factory.newInstance();
        for (FileBuilder fileBuilder : fileBuilders) {
            CryptedFile file = pavewayService.complete(fileBuilder);
            
            FileType fileXml = bundleType.addNewFile();
            fileXml.setName(file.getFileName());
            fileXml.setMimeType(file.getMimeType());
            fileXml.setUUID(file.getId().toString());
            fileXml.setKey(file.getSecretKey().getEncoded());
            fileXml.setLength(file.getOriginalLength());
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
    protected BundleType copyBundle(int maxDownloads, BundleType bundleType) {
        BundleType b = BundleType.Factory.newInstance();
        List<FileType> fileList = bundleType.getFileList();
        for (FileType file : fileList) {
            FileType f = b.addNewFile();
            f.setKey(file.getKey());
            f.setLength(file.getLength());
            f.setMimeType(file.getMimeType());
            f.setName(file.getName());
            f.setUUID(file.getUUID());
            f.setMaxDownloads(maxDownloads);
        }
        return b;
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
        allocationType.setBundle(bundleType);
        return doc;
    }
    
    protected void decryptDocument(Allocation allocation, byte[] secretKeyBytes) {
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
            allocationFile.setCryptedFileId(UUID.fromString(fileType.getUUID()));
            allocationFile.setXml(fileType);
            allocationFileDAO.create(allocationFile);
        }
    }
    
    
    protected void assignFileXml(Allocation allocation) {
        AllocationType allocationType = allocation.getXml();
        Map<UUID, AllocationFile> files = allocation.getFiles();
        List<FileType> fileList = allocationType.getBundle().getFileList();
        for (FileType fileType : fileList) {
            AllocationFile allocationFile = files.get(UUID.fromString(fileType.getUUID()));
            allocationFile.setXml(fileType);
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
        AccessorContext accessorContext = AccessorContext.getCurrent();
        accessorContext.retain(key, allocation);
        Set<Entry<UUID,AllocationFile>> fileEntrySet = allocation.getFiles().entrySet();
        for (Entry<UUID, AllocationFile> entry : fileEntrySet) {
            accessorContext.retain(entry.getKey(), entry.getValue());
        }
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.AllocationService#refreshAllocation(org.brekka.pegasus.core.model.AnonymousTransfer)
     */
    protected void refreshAllocation(Allocation allocation) {
        allocationDAO.refresh(allocation);
        assignFileXml(allocation);
    }
    

    /**
     * @param reference
     * @param allocationDocument
     */
    protected void setReference(String reference, AllocationDocument allocationDocument) {
        if (StringUtils.isNotBlank(reference)) {
            allocationDocument.getAllocation().setReference(reference);
        }
    }

    /**
     * @param agreementText
     * @param allocationDocument
     */
    protected void setAgreementText(String agreementText, AllocationDocument allocationDocument) {
        if (StringUtils.isNotBlank(agreementText)) {
            allocationDocument.getAllocation().setAgreement(agreementText);
        }
    }

    /**
     * @param comment
     * @param allocationDocument
     */
    protected void setComment(String comment, AllocationDocument allocationDocument) {
        if (StringUtils.isNotBlank(comment)) {
            allocationDocument.getAllocation().setComment(comment);
        }
    }
}