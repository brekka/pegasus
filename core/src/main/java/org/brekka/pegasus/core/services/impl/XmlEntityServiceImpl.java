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
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.brekka.paveway.core.model.Compression;
import org.brekka.paveway.core.services.ResourceEncryptor;
import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.dao.XmlEntityDAO;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.XmlEntity;
import org.brekka.pegasus.core.services.XmlEntityService;
import org.brekka.phalanx.api.model.CryptedData;
import org.brekka.phoenix.CryptoFactory;
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
public class XmlEntityServiceImpl extends PegasusServiceSupport implements XmlEntityService {

    @Autowired
    private XmlEntityDAO xmlEntityDAO;
    
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.XmlEntityService#persistPlainEntity(org.apache.xmlbeans.XmlObject)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public <T extends XmlObject> XmlEntity<T> persistPlainEntity(T xml) {
        XmlEntity<T> entity = new XmlEntity<>();
        entity.setBean(xml);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try ( GZIPOutputStream gos = new GZIPOutputStream(baos) ) {
            xml.save(gos);
            gos.close();
        } catch (IOException e) {
            throw new PegasusException(PegasusErrorCode.PG400, e, "Failed to persist XML");
        }
        byte[] data = baos.toByteArray();
        xmlEntityDAO.create(entity, new ByteArrayInputStream(data), data.length);
        return entity;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.XmlEntityService#persistEncryptedEntity(org.apache.xmlbeans.XmlObject, org.brekka.pegasus.core.model.KeySafe)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public <T extends XmlObject> XmlEntity<T> persistEncryptedEntity(T xml, KeySafe keySafe) {
        XmlEntity<T> entity = new XmlEntity<>();
        entity.setBean(xml);
        CryptoFactory cryptoFactory = cryptoFactoryRegistry.getDefault();
        SecretKey secretKey = cryptoFactory.getSymmetric().getKeyGenerator().generateKey();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ResourceEncryptor encryptor = resourceCryptoService.encryptor(secretKey, Compression.GZIP);
        OutputStream saveOs = encryptor.encrypt(baos);
        
        CryptedData cryptedData = keySafeService.protect(secretKey.getEncoded(), keySafe);
        
        entity.setIv(encryptor.getIV().getIV());
        entity.setCryptedDataId(cryptedData.getId());
        entity.setKeySafe(keySafe);
        entity.setProfile(cryptoFactory.getProfileId());
        
        try {
            xml.save(saveOs);
            saveOs.close();
        } catch (IOException e) {
            throw new PegasusException(PegasusErrorCode.PG400, e, "Failed to persist XML");
        }
        byte[] data = baos.toByteArray();
        xmlEntityDAO.create(entity, new ByteArrayInputStream(data), data.length);
        return entity;
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
    @SuppressWarnings("unchecked")
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public <T extends XmlObject> XmlEntity<T> retrieveEntity(UUID xmlEntityId, Class<T> expectedType) {
        XmlEntity<T> xmlEntity = (XmlEntity<T>) xmlEntityDAO.retrieveById(xmlEntityId);
        T xmlBean;
        InputStream is = null;
        try {
            is = xmlEntity.getData().getBinaryStream();
            UUID cryptedDataId = xmlEntity.getCryptedDataId();
            if (cryptedDataId != null) {
                // Decrypt
                CryptoFactory cryptoFactory = cryptoFactoryRegistry.getFactory(xmlEntity.getProfile());
                byte[] secretKeyBytes = keySafeService.release(cryptedDataId, xmlEntity.getKeySafe());
                SecretKey secretKey = new SecretKeySpec(secretKeyBytes, cryptoFactory.getSymmetric().getKeyGenerator().getAlgorithm());
                int cryptoProfileId = xmlEntity.getProfile();
                IvParameterSpec iv = new IvParameterSpec(xmlEntity.getIv());
                is = resourceCryptoService.decryptor(cryptoProfileId, Compression.GZIP, iv, secretKey, is);
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
                    "Failed to extract Xml Entity '%s'", xmlEntityId);
        } finally {
            IOUtils.closeQuietly(is);
        }
        xmlEntity.setBean(xmlBean);
        return xmlEntity;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.XmlEntityService#delete(java.util.UUID)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void delete(UUID xmlEntityId) {
        xmlEntityDAO.delete(xmlEntityId);
    }
}
