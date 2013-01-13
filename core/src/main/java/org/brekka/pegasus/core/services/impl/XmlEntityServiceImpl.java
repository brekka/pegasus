/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.brekka.commons.io.IterableStringReader;
import org.brekka.commons.io.StringListWriter;
import org.brekka.paveway.core.model.ByteSequence;
import org.brekka.paveway.core.model.Compression;
import org.brekka.paveway.core.model.ResourceEncryptor;
import org.brekka.paveway.core.services.ResourceCryptoService;
import org.brekka.paveway.core.services.ResourceStorageService;
import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.dao.XmlEntityDAO;
import org.brekka.pegasus.core.event.VaultDeleteEvent;
import org.brekka.pegasus.core.event.XmlEntityDeleteEvent;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.XmlEntity;
import org.brekka.pegasus.core.model.XmlEntityAware;
import org.brekka.pegasus.core.services.KeySafeService;
import org.brekka.pegasus.core.services.XmlEntityService;
import org.brekka.phalanx.api.beans.IdentityCryptedData;
import org.brekka.phalanx.api.model.CryptedData;
import org.brekka.phalanx.api.services.PhalanxService;
import org.brekka.phoenix.api.CryptoProfile;
import org.brekka.phoenix.api.SecretKey;
import org.brekka.phoenix.api.StreamCryptor;
import org.brekka.phoenix.api.SymmetricCryptoSpec;
import org.brekka.phoenix.api.services.CryptoProfileService;
import org.brekka.phoenix.api.services.SymmetricCryptoService;
import org.brekka.stillingar.api.annotations.Configured;
import org.brekka.xml.pegasus.v2.config.XmlEntityServiceDocument;
import org.brekka.xml.pegasus.v2.config.XmlEntityServiceDocument.XmlEntityService.NamespacePrefix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import difflib.DiffUtils;
import difflib.PatchFailedException;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Service
@Transactional
@Configured
public class XmlEntityServiceImpl implements XmlEntityService, ApplicationListener<ApplicationEvent>  {

    @Autowired
    private XmlEntityDAO xmlEntityDAO;

    @Autowired
    private SymmetricCryptoService symmetricCryptoService;
    
    @Autowired
    private CryptoProfileService cryptoProfileService;
    
    @Autowired
    private ResourceCryptoService resourceCryptoService;
    
    @Autowired
    private ResourceStorageService resourceStorageService;
    
    @Autowired
    private KeySafeService keySafeService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    
    @Autowired
    private PhalanxService phalanxService;
    
    private XmlOptions xmlWriteOptions;
    
    
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.XmlEntityService#persistPlainEntity(org.apache.xmlbeans.XmlObject)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public <T extends XmlObject> XmlEntity<T> persistPlainEntity(T xml, boolean externalData) {
        XmlEntity<T> entity = createPlainEntity(xml, 1, UUID.randomUUID(), externalData);
        return entity;
    }
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public <T extends XmlObject> XmlEntity<T> release(XmlEntity<T> theEntity, Class<T> expectedType) {
        if (theEntity.getBean() != null) {
            return theEntity;
        }
        XmlEntity<T> entity = retrieveEntity(theEntity.getId(), expectedType);
        return entity;
    }
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public <T extends XmlObject> XmlEntity<T> release(XmlEntity<T> theEntity, String password, Class<T> expectedType) {
        if (theEntity.getBean() != null) {
            return theEntity;
        }
        XmlEntity<T> entity = retrieveEntity(theEntity.getId(), expectedType, password);
        return entity;
    }
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public <T extends XmlObject> void releaseAll(List<? extends XmlEntityAware<T>> list, Class<T> expectedType) {
        for (XmlEntityAware<T> xmlEntityAware : list) {
            // TODO parallel
            release(xmlEntityAware, expectedType);
        }
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.XmlEntityService#release(org.brekka.pegasus.core.model.XmlEntityAware, java.lang.Class)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public <T extends XmlObject> void release(XmlEntityAware<T> entity, Class<T> expectedType) {
        XmlEntity<?> xml = entity.getXml();
        if (xml.getBean() == null) {
            XmlEntity<T> released = retrieveEntity(xml.getId(), expectedType);
            entity.setXml(released);
        }
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.XmlEntityService#persistEncryptedEntity(org.apache.xmlbeans.XmlObject, org.brekka.pegasus.core.model.KeySafe)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public <T extends XmlObject> XmlEntity<T> persistEncryptedEntity(T xml, KeySafe<?> keySafe, boolean externalData) {
        CryptoProfile cryptoProfile = cryptoProfileService.retrieveDefault();
        SecretKey secretKey = symmetricCryptoService.createSecretKey(cryptoProfile);
        CryptedData cryptedData = keySafeService.protect(secretKey.getEncoded(), keySafe);
        XmlEntity<T> entity = createEncrypted(xml, 1, UUID.randomUUID(), keySafe, cryptedData.getId(), cryptoProfile, secretKey, externalData);
        return entity;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.XmlEntityService#persistEncryptedEntity(org.apache.xmlbeans.XmlObject, org.brekka.pegasus.core.model.KeySafe)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public <T extends XmlObject> XmlEntity<T> persistEncryptedEntity(T xml, String password, boolean externalData) {
        CryptoProfile cryptoProfile = cryptoProfileService.retrieveDefault();
        SecretKey secretKey = symmetricCryptoService.createSecretKey(cryptoProfile);
        CryptedData cryptedData = phalanxService.pbeEncrypt(secretKey.getEncoded(), password);
        XmlEntity<T> entity = createEncrypted(xml, 1, UUID.randomUUID(), null, cryptedData.getId(), cryptoProfile, secretKey, externalData);
        return entity;
    }


    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.XmlEntityService#updateEntity(org.brekka.pegasus.core.model.XmlEntity, org.brekka.pegasus.core.model.XmlEntity, java.lang.Class)
     */
    @Override
    public <T extends XmlObject> XmlEntity<T> updateEntity(XmlEntity<T> updated, XmlEntity<T> lockedCurrent, Class<T> xmlType) {
        if (updated == null) {
            // Request to remove the XML. Delete the series
            List<XmlEntity<?>> series = xmlEntityDAO.retrieveBySerial(lockedCurrent.getSerial());
            for (XmlEntity<?> xmlEntity : series) {
                xmlEntity.setDeleted(new Date());
                xmlEntityDAO.update(xmlEntity);
            }
            return null;
        }
        if (lockedCurrent.getKeySafe() == null 
                && lockedCurrent.getCryptedDataId() != null) {
            throw new PegasusException(PegasusErrorCode.PG443, 
                    "Only XML entities encrypted with a keySafe can be updated. " +
                    "Entity '%s' does not have a keySafe.", lockedCurrent.getId());
        }
        T xmlBean;
        if (updated.getVersion() == lockedCurrent.getVersion()) {
            // There is no intermediate update.
            xmlBean = updated.getBean();
        } else {
            // Need to diff
            
            // Retrieve the version we expected to be the last, to generate a diff from
            XmlEntity<T> updatedFrom = xmlEntityDAO.retrieveBySerialVersion(updated.getSerial(), updated.getVersion(), xmlType);
            // Passworded entity xml should never be updated.
            T base = extractXml(updatedFrom, xmlType, null);
            T current = extractXml(lockedCurrent, xmlType, null);
            xmlBean = threeWayDiff(base, current, updated.getBean(), xmlType);
        }
        
        XmlEntity<T> newEntity;
        UUID serial = lockedCurrent.getSerial();
        int newVersion = lockedCurrent.getVersion() + 1;
        KeySafe<?> keySafe = lockedCurrent.getKeySafe();
        boolean externalData = lockedCurrent.isExternalData();
        
        if (keySafe == null) {
            newEntity = createPlainEntity(xmlBean, newVersion, serial, externalData);
        } else {
            UUID cryptedDataId = lockedCurrent.getCryptedDataId();
            CryptoProfile cryptoProfile = cryptoProfileService.retrieveProfile(lockedCurrent.getProfile());
            byte[] secretKeyBytes = keySafeService.release(cryptedDataId, lockedCurrent.getKeySafe());
            SecretKey secretKey = symmetricCryptoService.toSecretKey(secretKeyBytes, cryptoProfile);
            // Currently using the same crypted Id.
            newEntity = createEncrypted(xmlBean, newVersion, serial, keySafe, cryptedDataId, cryptoProfile, secretKey, externalData);
        }
        return newEntity;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.XmlEntityService#isEncrypted(java.util.UUID)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public boolean isEncrypted(UUID xmlEntityId) {
        XmlEntity<?> xmlEntity = xmlEntityDAO.retrieveById(xmlEntityId);
        return xmlEntity != null 
             && xmlEntity.getCryptedDataId() != null;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.XmlEntityService#retrieveEntity(java.util.UUID)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public <T extends XmlObject> XmlEntity<T> retrieveEntity(UUID xmlEntityId, Class<T> expectedType) {
        return retrieveEntity(xmlEntityId, expectedType, null);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.XmlEntityService#delete(java.util.UUID)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void delete(UUID xmlEntityId) {
        XmlEntity<?> entity = xmlEntityDAO.retrieveById(xmlEntityId);
        if (entity.isExternalData()) {
            resourceStorageService.remove(entity.getId());
        }
        xmlEntityDAO.delete(xmlEntityId);
    }
    
    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof VaultDeleteEvent) {
            VaultDeleteEvent vaultDeleteEvent = (VaultDeleteEvent) event;
            List<XmlEntity<?>> xmlEntityList = xmlEntityDAO.retrieveByKeySafe(vaultDeleteEvent.getVault());
            for (XmlEntity<?> xmlEntity : xmlEntityList) {
                applicationEventPublisher.publishEvent(new XmlEntityDeleteEvent(xmlEntity));
                phalanxService.deleteCryptedData(new IdentityCryptedData(xmlEntity.getCryptedDataId()));
                xmlEntityDAO.delete(xmlEntity.getId());
            }
        }
    }
    
    @Configured
    public void configured(@Configured XmlEntityServiceDocument.XmlEntityService config) {
        Map<String, String> prefixes = new HashMap<>();
        List<NamespacePrefix> prefixList = config.getNamespacePrefixList();
        for (NamespacePrefix namespacePrefix : prefixList) {
            prefixes.put(namespacePrefix.getUri(), namespacePrefix.getPrefix());
        }
        XmlOptions opts = new XmlOptions();
        opts.setSavePrettyPrint();
        opts.setSaveAggressiveNamespaces();
        opts.setSaveSuggestedPrefixes(prefixes);
        opts.setSaveNamespacesFirst();
        this.xmlWriteOptions = opts;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.XmlEntityService#retrieveEntity(java.util.UUID)
     */
    @SuppressWarnings("unchecked")
    protected <T extends XmlObject> XmlEntity<T> retrieveEntity(UUID xmlEntityId, Class<T> expectedType, String password) {
        XmlEntity<T> xmlEntity = (XmlEntity<T>) xmlEntityDAO.retrieveById(xmlEntityId);
        T xmlBean = extractXml(xmlEntity, expectedType, password);
        xmlEntity.setBean(xmlBean);
        return xmlEntity;
    }
    
    /**
     * @param xml
     * @param keySafe
     * @param entity
     * @param cryptoProfile
     * @param secretKey
     */
    protected <T extends XmlObject> XmlEntity<T> createEncrypted(T xml, int version, UUID serial, KeySafe<?> keySafe, UUID cryptedDataId,
            CryptoProfile cryptoProfile, SecretKey secretKey, boolean externalData) {
        
        validate(xml);

        ResourceEncryptor encryptor = resourceCryptoService.encryptor(secretKey, Compression.GZIP);
        XmlEntity<T> entity = new XmlEntity<>();
        entity.setId(UUID.randomUUID());
        entity.setIv(encryptor.getSpec().getIV());
        entity.setCryptedDataId(cryptedDataId);
        entity.setKeySafe(keySafe);
        entity.setProfile(cryptoProfile.getNumber());
        entity.setExternalData(externalData);
        populate(entity, xml, version, serial);
        if (externalData) {
            ByteSequence allocate = resourceStorageService.allocate(entity.getId());
            try ( OutputStream os = allocate.getOutputStream(); OutputStream saveOs = encryptor.encrypt(os)) {
                xml.save(saveOs, xmlWriteOptions);
                saveOs.close();
            } catch (IOException e) {
                throw new PegasusException(PegasusErrorCode.PG400, e, "Failed to persist XML");
            }
            xmlEntityDAO.create(entity);
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (OutputStream saveOs = encryptor.encrypt(baos)) {
                xml.save(saveOs, xmlWriteOptions);
                saveOs.close();
            } catch (IOException e) {
                throw new PegasusException(PegasusErrorCode.PG400, e, "Failed to persist XML");
            }
            byte[] data = baos.toByteArray();
            xmlEntityDAO.create(entity, new ByteArrayInputStream(data), data.length);
        }
        return entity;
    }
    
    /**
     * @param xml
     * @param entity
     */
    protected <T extends XmlObject> XmlEntity<T> createPlainEntity(T xml, int version, UUID serial, boolean externalData) {
        validate(xml);
        
        XmlEntity<T> entity = new XmlEntity<>();
        entity.setId(UUID.randomUUID());
        populate(entity, xml, version, serial);
        entity.setExternalData(externalData);
        if (externalData) {
            ByteSequence allocate = resourceStorageService.allocate(entity.getId());
            try ( OutputStream os = allocate.getOutputStream(); 
                    GZIPOutputStream gos = new GZIPOutputStream(os) ) {
                xml.save(gos, xmlWriteOptions);
                gos.close();
            } catch (IOException e) {
                throw new PegasusException(PegasusErrorCode.PG400, e, "Failed to persist XML");
            }
            xmlEntityDAO.create(entity);
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try ( GZIPOutputStream gos = new GZIPOutputStream(baos) ) {
                xml.save(gos, xmlWriteOptions);
                gos.close();
            } catch (IOException e) {
                throw new PegasusException(PegasusErrorCode.PG400, e, "Failed to persist XML");
            }
            byte[] data = baos.toByteArray();
            xmlEntityDAO.create(entity, new ByteArrayInputStream(data), data.length);
        }
        
        return entity;
    }
    
    protected <T extends XmlObject> void populate(XmlEntity<T> entity, T xml, int version, UUID serial) {
        entity.setVersion(version);
        entity.setSerial(serial);
        entity.setBean(xml);
    }
    

    /**
     * @param xmlEntity
     * @param expectedType
     * @return
     */
    @SuppressWarnings("unchecked")
    protected <T extends XmlObject> T extractXml(XmlEntity<T> xmlEntity, Class<T> expectedType, String password) {
        T xmlBean;
        InputStream is = null;
        try {
            if (xmlEntity.isExternalData()) {
                ByteSequence byteSequence = resourceStorageService.retrieve(xmlEntity.getId());
                is = byteSequence.getInputStream();
            } else {
                is = xmlEntity.getData().getBinaryStream();
            }
            UUID cryptedDataId = xmlEntity.getCryptedDataId();
            if (cryptedDataId != null) {
                // Decrypt
                CryptoProfile cryptoProfile = cryptoProfileService.retrieveProfile(xmlEntity.getProfile());
                byte[] secretKeyBytes;
                if (xmlEntity.getKeySafe() == null) {
                    // Expect password
                    if (password != null) {
                        secretKeyBytes = phalanxService.pbeDecrypt(new IdentityCryptedData(xmlEntity.getCryptedDataId()), password);
                    } else {
                        throw new PegasusException(PegasusErrorCode.PG423, 
                                "The XML entity '%s' must be unlocked using a password, which was not specified.", 
                                xmlEntity.getId());
                    }
                } else {
                    secretKeyBytes = keySafeService.release(cryptedDataId, xmlEntity.getKeySafe());
                }
                SecretKey secretKey = symmetricCryptoService.toSecretKey(secretKeyBytes, cryptoProfile);
                xmlEntity.setSecretKey(secretKey);
                StreamCryptor<InputStream, SymmetricCryptoSpec> decryptor = resourceCryptoService.decryptor(xmlEntity, Compression.GZIP);
                is = decryptor.getStream(is);
            } else {
                // Plain
                is = new GZIPInputStream(is);
            }
            
            SchemaType schemaType = XmlBeans.typeForClass(expectedType);
            xmlBean = (T) XmlBeans.getContextTypeLoader().parse(is, schemaType, null);
            if (!expectedType.isAssignableFrom(xmlBean.getClass())) {
                throw new PegasusException(PegasusErrorCode.PG401, 
                        "Expected '%s', found '%s'", 
                        expectedType.getName(), xmlBean.getClass().getName());
            }
            
        } catch (IOException | SQLException | XmlException e) {
            throw new PegasusException(PegasusErrorCode.PG401, e, 
                    "Failed to extract Xml Entity '%s'", xmlEntity.getId());
        } finally {
            IOUtils.closeQuietly(is);
        }
        return xmlBean;
    }
    
    
    @SuppressWarnings("unchecked")
    protected <T extends XmlObject> T threeWayDiff(T base, T current, T update, Class<T> type) {
        
        T obj;
        List<String> baseStrList = toStringList(base);
        List<String> currentStrList = toStringList(current);
        List<String> updateStrList = toStringList(update);
        
        difflib.Patch patch = DiffUtils.diff(baseStrList, updateStrList);
        List<String> patched;
        try {
            patched = (List<String>) DiffUtils.patch(currentStrList, patch);
        } catch (PatchFailedException e) {
            throw new PegasusException(PegasusErrorCode.PG403, e, "Patch conflict");
        }
        
        SchemaTypeLoader contextTypeLoader = XmlBeans.getContextTypeLoader();
        SchemaType schemaType = XmlBeans.typeForClass(type);
        try {
            obj = (T) contextTypeLoader.parse(new IterableStringReader(patched), schemaType, new XmlOptions());
        } catch (XmlException e) {
            throw new PegasusException(PegasusErrorCode.PG402, e, "Patch resulted in invalid XML");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return obj;
    }
    
    protected List<String> toStringList(XmlObject obj) {
        StringListWriter out = new StringListWriter();
        try {
            obj.save(out, xmlWriteOptions);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return out.toList();
    }
    
    protected void validate(XmlObject xml) {
        if (!xml.validate()) {
            // TODO more detail
            throw new PegasusException(PegasusErrorCode.PG333, "XML does not validate");
        }
    }
}
