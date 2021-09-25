/*
 * Copyright 2012 the original author or authors.
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

import static java.lang.String.format;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlError;
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
import org.brekka.pegasus.core.utils.DelayedOutputStream;
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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import difflib.DiffUtils;
import difflib.PatchFailedException;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Service
@Configured
public class XmlEntityServiceImpl implements XmlEntityService, ApplicationListener<ApplicationEvent>  {

    private static final Log log = LogFactory.getLog(XmlEntityServiceImpl.class);

    /**
     * If the compressed payload is less than this size, store in the database, otherwise use the ResourceStorageService
     */
    private static final int DEFAULT_SMALL_CONTENT_LIMIT = 32768;

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

    /**
     * The threshold (in number of bytes) at which content should be written to ResourceStorageService.
     */
    private int smallContentLimit = DEFAULT_SMALL_CONTENT_LIMIT;


    @Override
    @Transactional(readOnly=true)
    public <T extends XmlObject> XmlEntity<T> release(final XmlEntity<T> theEntity, final Class<T> expectedType) {
        if (theEntity.getBean() != null) {
            return theEntity;
        }
        XmlEntity<T> entity = retrieveEntity(theEntity.getId(), expectedType);
        return entity;
    }

    @Override
    @Transactional(readOnly=true)
    public <T extends XmlObject> XmlEntity<T> release(final XmlEntity<T> theEntity, final String password,
            final Class<T> expectedType) {

        if (theEntity.getBean() != null) {
            return theEntity;
        }
        XmlEntity<T> entity = retrieveEntity(theEntity.getId(), expectedType, password);
        return entity;
    }

    @Override
    @Transactional(readOnly=true)
    public <T extends XmlObject> void releaseAll(final List<? extends XmlEntityAware<T>> list,
            final Class<T> expectedType) {

        for (XmlEntityAware<T> xmlEntityAware : list) {
            // TODO parallel
            release(xmlEntityAware, expectedType);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public <T extends XmlObject> void release(final XmlEntityAware<T> entity, final Class<T> expectedType) {
        if (entity == null) {
            // No entity, nothing to release.
            return;
        }
        XmlEntity<?> xml = entity.getXml();
        if (xml == null) {
            // No XML, can't release that either
            return;
        }
        if (xml.getBean() == null) {
            XmlEntity<T> released = retrieveEntity(xml.getId(), expectedType);
            entity.setXml(released);
        }
    }

    @Deprecated
    @Override
    @Transactional()
    public <T extends XmlObject> XmlEntity<T> persistPlainEntity(final T xml, final boolean externalData) {
        return persistPlainEntity(xml);
    }

    @Override
    @Transactional()
    public <T extends XmlObject> XmlEntity<T> persistPlainEntity(final T xml) {
        XmlEntity<T> entity = createPlainEntity(xml, 1, UUID.randomUUID());
        return entity;
    }

    @Deprecated
    @Override
    @Transactional()
    public <T extends XmlObject> XmlEntity<T> persistEncryptedEntity(final T xml, final KeySafe<?> keySafe,
            final boolean externalData) {

        return persistEncryptedEntity(xml, keySafe);
    }

    @Override
    @Transactional()
    public <T extends XmlObject> XmlEntity<T> persistEncryptedEntity(final T xml, final KeySafe<?> keySafe) {
        CryptoProfile cryptoProfile = this.cryptoProfileService.retrieveDefault();
        SecretKey secretKey = this.symmetricCryptoService.createSecretKey(cryptoProfile);
        CryptedData cryptedData = this.keySafeService.protect(secretKey.getEncoded(), keySafe);
        XmlEntity<T> entity = createEncrypted(xml, 1, UUID.randomUUID(), keySafe, cryptedData.getId(), cryptoProfile,
                secretKey);

        return entity;
    }

    @Deprecated
    @Override
    @Transactional()
    public <T extends XmlObject> XmlEntity<T> persistEncryptedEntity(final T xml, final String password,
            final boolean externalData) {

        return persistEncryptedEntity(xml, password);
    }

    @Override
    @Transactional()
    public <T extends XmlObject> XmlEntity<T> persistEncryptedEntity(final T xml, final String password) {
        CryptoProfile cryptoProfile = this.cryptoProfileService.retrieveDefault();
        SecretKey secretKey = this.symmetricCryptoService.createSecretKey(cryptoProfile);
        CryptedData cryptedData = this.phalanxService.pbeEncrypt(secretKey.getEncoded(), password);
        XmlEntity<T> entity = createEncrypted(xml, 1, UUID.randomUUID(), null, cryptedData.getId(), cryptoProfile,
                secretKey);

        return entity;
    }

    @Override
    @Transactional(isolation=Isolation.SERIALIZABLE)
    public <T extends XmlObject> XmlEntity<T> updateEntity(final XmlEntity<T> updated, final XmlEntity<T> lockedCurrent,
            final Class<T> xmlType) {

        if (updated == null) {
            // Request to remove the XML. Delete the series
            List<XmlEntity<?>> series = this.xmlEntityDAO.retrieveBySerial(lockedCurrent.getSerial());
            for (XmlEntity<?> xmlEntity : series) {
                xmlEntity.setDeleted(new Date());
                this.xmlEntityDAO.update(xmlEntity);
            }
            return null;
        }
        if (lockedCurrent.getKeySafe() == null
                && lockedCurrent.getCryptedDataId() != null) {
            throw new PegasusException(PegasusErrorCode.PG443,
                    "Only XML entities encrypted with a keySafe can be updated. " +
                    "Entity '%s' does not have a keySafe.", lockedCurrent.getId());
        }
        T base;
        T current;
        if (updated.getVersion() == lockedCurrent.getVersion()) {
            // There is no intermediate update, base and current will be the same
            base = extractXml(lockedCurrent, xmlType, null);
            current = base;
        } else {
            // Retrieve the version we expected to be the last, to generate a diff from
            XmlEntity<T> updatedFrom = this.xmlEntityDAO.retrieveBySerialVersion(updated.getSerial(),
                    updated.getVersion(), xmlType);

            // Passworded entity xml should never be updated.
            base = extractXml(updatedFrom, xmlType, null);
            current = extractXml(lockedCurrent, xmlType, null);
        }
        T patched = threeWayDiff(base, current, updated.getBean(), xmlType);
        if (patched == null) {
            // There is no difference from the locked current, just return it and save creating another version
            return lockedCurrent;
        }

        XmlEntity<T> newEntity;
        UUID serial = lockedCurrent.getSerial();
        int newVersion = lockedCurrent.getVersion() + 1;
        KeySafe<?> keySafe = lockedCurrent.getKeySafe();

        if (keySafe == null) {
            newEntity = createPlainEntity(patched, newVersion, serial);
        } else {
            UUID cryptedDataId = lockedCurrent.getCryptedDataId();
            CryptoProfile cryptoProfile = this.cryptoProfileService.retrieveProfile(lockedCurrent.getProfile());
            byte[] secretKeyBytes = this.keySafeService.release(cryptedDataId, lockedCurrent.getKeySafe());
            SecretKey secretKey = this.symmetricCryptoService.toSecretKey(secretKeyBytes, cryptoProfile);
            // Currently using the same crypted Id.
            newEntity = createEncrypted(patched, newVersion, serial, keySafe, cryptedDataId, cryptoProfile, secretKey);
        }
        return newEntity;
    }

    @Override
    @Transactional
    public <T extends XmlObject>  XmlEntity<T> applyEncryption(final XmlEntity<T> xml, final KeySafe<?> keySafe,
            final Class<T> xmlType) {

        XmlEntity<T> managed = retrieveEntity(xml.getId(), xmlType);
        CryptoProfile cryptoProfile = this.cryptoProfileService.retrieveDefault();
        SecretKey secretKey = this.symmetricCryptoService.createSecretKey(cryptoProfile);
        CryptedData cryptedData = this.keySafeService.protect(secretKey.getEncoded(), keySafe);

        UUID serial = managed.getSerial();
        int newVersion = managed.getVersion() + 1;

        return createEncrypted(managed.getBean(), newVersion, serial, keySafe, cryptedData.getId(), cryptoProfile,
                secretKey);
    }

    @Override
    @Transactional
    public <T extends XmlObject>  XmlEntity<T> removeEncryption(final XmlEntity<T> xml, final Class<T> xmlType) {
        XmlEntity<T> managed = release(xml, xmlType);
        UUID serial = managed.getSerial();
        int newVersion = managed.getVersion() + 1;
        return createPlainEntity(managed.getBean(), newVersion, serial);
    }

    @Override
    @Transactional()
    public boolean isEncrypted(final UUID xmlEntityId) {
        XmlEntity<?> xmlEntity = this.xmlEntityDAO.retrieveById(xmlEntityId);
        return xmlEntity != null
             && xmlEntity.getCryptedDataId() != null;
    }

    @Override
    @Transactional()
    public <T extends XmlObject> XmlEntity<T> retrieveEntity(final UUID xmlEntityId, final Class<T> expectedType) {
        return retrieveEntity(xmlEntityId, expectedType, null);
    }

    @Override
    @Transactional()
    public void delete(final UUID xmlEntityId) {
        XmlEntity<?> entity = xmlEntityDAO.retrieveById(xmlEntityId);
        if (entity.isExternalData()) {
            // Make sure the resource is only removed when actually committed.
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    log.info(format("Permanently removing resource for deleted XmlEntity '%s'", entity.getId()));
                    resourceStorageService.remove(entity.getId());
                }
            });
        }
        xmlEntityDAO.delete(xmlEntityId);
    }

    @Override
    public void onApplicationEvent(final ApplicationEvent event) {
        if (event instanceof VaultDeleteEvent) {
            VaultDeleteEvent vaultDeleteEvent = (VaultDeleteEvent) event;
            List<XmlEntity<?>> xmlEntityList = this.xmlEntityDAO.retrieveByKeySafe(vaultDeleteEvent.getVault());
            for (XmlEntity<?> xmlEntity : xmlEntityList) {
                this.applicationEventPublisher.publishEvent(new XmlEntityDeleteEvent(xmlEntity));
                this.phalanxService.deleteCryptedData(new IdentityCryptedData(xmlEntity.getCryptedDataId()));
                this.xmlEntityDAO.delete(xmlEntity.getId());
            }
        }
    }

    @Configured
    public void configured(@Configured final XmlEntityServiceDocument.XmlEntityService config) {
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

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UUID updateStorage(final UUID afterId, final int limit) {
        List<XmlEntity<?>> results = xmlEntityDAO.findExternalResourceBased(afterId, limit);
        log.info(format("Found %d XmlEntity record(s) with external resources and ids after '%s' (limit %d)",
                results.size(), afterId, limit));

        UUID lastId = null;
        Set<UUID> resourcesToDelete = new LinkedHashSet<>();
        for (XmlEntity<?> entity : results) {
            lastId = entity.getId();
            ByteSequence sequence = resourceStorageService.retrieve(entity.getId());
            if (sequence == null) {
                log.warn(format("Failed to locate resource file for XmlEntity '%s'", entity.getId()));
                continue;
            }
            @SuppressWarnings("resource")
            CountingOutputStream cos = new CountingOutputStream(NullOutputStream.NULL_OUTPUT_STREAM);
            DelayedOutputStream dos = new DelayedOutputStream(smallContentLimit, () -> cos);

            try (InputStream is = sequence.getInputStream();
                    OutputStream os = dos) {

                IOUtils.copy(is, os);
            } catch (IOException e) {
                log.warn(format("Problem reading external content for XmlEntity '%s'", entity.getId()), e);
                continue;
            }
            if (dos.isInMemory()) {
                // Just moving the resource bytes onto the record, no need to change it in any way
                byte[] data = dos.getInMemoryBytes();
                entity.setData(data);
                entity.setExternalData(false);
                xmlEntityDAO.update(entity);
                resourcesToDelete.add(entity.getId());
                log.info(format("Moving XmlEntity '%s' content from external resource to database (%d bytes)",
                        entity.getId(), data.length));
            } else {
                log.info(format("Ignoring XmlEntity '%s' as content is %d bytes", entity.getId(), cos.getByteCount()));
            }
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                log.info(format("Requesting removal of %d resources", resourcesToDelete.size()));
                // Must ONLY delete these resources after successful commit!!!
                resourcesToDelete.forEach(resourceStorageService::remove);
            }
        });
        return lastId;
    }

    @SuppressWarnings("unchecked")
    private <T extends XmlObject> XmlEntity<T> retrieveEntity(final UUID xmlEntityId, final Class<T> expectedType,
            final String password) {

        XmlEntity<T> xmlEntity = (XmlEntity<T>) this.xmlEntityDAO.retrieveById(xmlEntityId);
        T xmlBean = extractXml(xmlEntity, expectedType, password);
        xmlEntity.setBean(xmlBean);
        return xmlEntity;
    }

    private <T extends XmlObject> XmlEntity<T> createEncrypted(final T xml, final int version, final UUID serial,
            final KeySafe<?> keySafe, final UUID cryptedDataId, final CryptoProfile cryptoProfile,
            final SecretKey secretKey) {

        validate(xml);

        ResourceEncryptor encryptor = resourceCryptoService.encryptor(secretKey, Compression.GZIP);
        XmlEntity<T> entity = new XmlEntity<>();
        entity.setId(UUID.randomUUID());
        entity.setIv(encryptor.getSpec().getIv());
        entity.setCryptedDataId(cryptedDataId);
        entity.setKeySafe(keySafe);
        entity.setProfile(cryptoProfile.getNumber());
        populate(entity, xml, version, serial);
        DelayedOutputStream os = new DelayedOutputStream(smallContentLimit,
            () -> resourceStorageService.allocate(entity.getId()).getOutputStream());

        try (OutputStream saveOs = encryptor.encrypt(os)) {
            xml.save(saveOs, xmlWriteOptions);
        } catch (IOException e) {
            throw new PegasusException(PegasusErrorCode.PG400, e, "Failed to persist XML");
        }
        if (os.isInMemory()) {
            entity.setData(os.getInMemoryBytes());
            entity.setExternalData(false);
        } else {
            entity.setExternalData(true);
        }
        xmlEntityDAO.create(entity);
        return entity;
    }

    private <T extends XmlObject> XmlEntity<T> createPlainEntity(final T xml, final int version, final UUID serial) {

        validate(xml);

        XmlEntity<T> entity = new XmlEntity<>();
        entity.setId(UUID.randomUUID());
        populate(entity, xml, version, serial);
        DelayedOutputStream os = new DelayedOutputStream(smallContentLimit,
                () -> resourceStorageService.allocate(entity.getId()).getOutputStream());

        try (GZIPOutputStream gos = new GZIPOutputStream(os) ) {
            xml.save(gos, xmlWriteOptions);
        } catch (IOException e) {
            throw new PegasusException(PegasusErrorCode.PG400, e, "Failed to persist XML");
        }
        if (os.isInMemory()) {
            entity.setData(os.getInMemoryBytes());
            entity.setExternalData(false);
        } else {
            entity.setExternalData(true);
        }
        xmlEntityDAO.create(entity);
        return entity;
    }

    private <T extends XmlObject> void populate(final XmlEntity<T> entity, final T xml, final int version,
            final UUID serial) {

        entity.setVersion(version);
        entity.setSerial(serial);
        entity.setBean(xml);
    }

    @SuppressWarnings("unchecked")
    private <T extends XmlObject> T extractXml(final XmlEntity<T> xmlEntity, final Class<T> expectedType,
            final String password) {

        T xmlBean;
        InputStream is = null;
        try {
            if (xmlEntity.isExternalData()) {
                ByteSequence byteSequence = this.resourceStorageService.retrieve(xmlEntity.getId());
                if (byteSequence == null) {
                    throw new PegasusException(PegasusErrorCode.PG100,
                            "No entity XML file found with the id '%s'", xmlEntity.getId());
                }
                is = byteSequence.getInputStream();
            } else {
                is = new ByteArrayInputStream(xmlEntity.getData());
            }
            UUID cryptedDataId = xmlEntity.getCryptedDataId();
            if (cryptedDataId != null) {
                // Decrypt
                CryptoProfile cryptoProfile = this.cryptoProfileService.retrieveProfile(xmlEntity.getProfile());
                byte[] secretKeyBytes;
                if (xmlEntity.getKeySafe() == null) {
                    // Expect password
                    if (password != null) {
                        secretKeyBytes = this.phalanxService.pbeDecrypt(
                                new IdentityCryptedData(xmlEntity.getCryptedDataId()), password);
                    } else {
                        throw new PegasusException(PegasusErrorCode.PG423,
                                "The XML entity '%s' must be unlocked using a password, which was not specified.",
                                xmlEntity.getId());
                    }
                } else {
                    secretKeyBytes = this.keySafeService.release(cryptedDataId, xmlEntity.getKeySafe());
                }
                SecretKey secretKey = this.symmetricCryptoService.toSecretKey(secretKeyBytes, cryptoProfile);
                xmlEntity.setSecretKey(secretKey);
                StreamCryptor<InputStream, SymmetricCryptoSpec> decryptor =
                        resourceCryptoService.decryptor(xmlEntity, Compression.GZIP);

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

        } catch (IOException | XmlException e) {
            throw new PegasusException(PegasusErrorCode.PG401, e,
                    "Failed to extract Xml Entity '%s'", xmlEntity.getId());
        } finally {
            IOUtils.closeQuietly(is);
        }
        return xmlBean;
    }


    @SuppressWarnings("unchecked")
    private <T extends XmlObject> T threeWayDiff(final T base, final T current, final T update, final Class<T> type) {

        T obj;
        List<String> baseStrList = toStringList(base);
        List<String> currentStrList = toStringList(current);
        List<String> updateStrList = toStringList(update);

        difflib.Patch patch = DiffUtils.diff(baseStrList, updateStrList);
        if (patch.getDeltas().isEmpty()) {
            // No change
            return null;
        }
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
            throw new PegasusException(PegasusErrorCode.PG402, e, "Patch resulted in invalid XML: %s%n",
                    StringUtils.join(patched, '\n'));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return obj;
    }

    private List<String> toStringList(final XmlObject obj) {
        StringListWriter out = new StringListWriter();
        try {
            obj.save(out, this.xmlWriteOptions);
            out.close();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return out.toList();
    }

    private void validate(final XmlObject xml) {
        XmlOptions xo = new XmlOptions();
        List<XmlError> errors = new ArrayList<>();
        xo.setErrorListener(errors);
        if (xml.validate(xo)) {
            // All good
            return;
        }
        if (log.isDebugEnabled()) {
            // Need to be careful with this, XML can include sensitive information
            StringWriter sw = new StringWriter();
            try (PrintWriter out = new PrintWriter(sw)) {
                out.printf("XML Validation errors (%d)%n", errors.size());
                int cnt = 1;
                for (XmlError xmlError : errors) {
                    out.printf("\t%d) line %d, col %d: %s%n", cnt, xmlError.getLine(), xmlError.getColumn(),
                            xmlError.getMessage());
                    cnt++;
                }
                out.println("Content:");
                out.flush();
                XmlOptions opts = new XmlOptions(this.xmlWriteOptions);
                opts.setSaveNoXmlDecl();
                xml.save(out, opts);
            } catch (IOException e) {
                log.warn("IO Error", e);
            }
            log.debug(sw.toString());
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < errors.size(); i++) {
            if (i != 0) {
                sb.append(", ");
            }
            XmlError xmlError = errors.get(i);
            sb.append(String.format("%d) line %d, col %d: %s%n",
                    (i + 1), xmlError.getLine(), xmlError.getColumn(), xmlError.getMessage()));
        }
        throw new PegasusException(PegasusErrorCode.PG333, "XML validation errors { %s }", sb);
    }

    public void setSmallContentLimit(final int smallContentLimit) {
        this.smallContentLimit = smallContentLimit;
    }
}
