/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.UUID;

import org.apache.xmlbeans.XmlObject;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.XmlEntity;

/**
 * Manages the storage of XML entities.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface XmlEntityService {

    /**
     * Store the specified XML in the database without encrypting it.
     * @param xml
     * @return
     */
    <T extends XmlObject> XmlEntity<T> persistPlainEntity(T xml, boolean useResourceStorageService);
    
    /**
     * Store the specified XML in the database, making sure to encrypt it and protect the key with
     * the specified cryptoStore.
     * @param xml
     * @param keySafe
     * @return
     */
    <T extends XmlObject> XmlEntity<T> persistEncryptedEntity(T xml, KeySafe<?> keySafe, boolean useResourceStorageService);
    
    <T extends XmlObject> XmlEntity<T> persistEncryptedEntity(T xml, String password, boolean externalData);
    
    <T extends XmlObject> XmlEntity<T> updateEntity(XmlEntity<T> updated, XmlEntity<T> lockedCurrent, Class<T> xmlType);
    
    <T extends XmlObject> XmlEntity<T> release(XmlEntity<T> theEntity, Class<T> expectedType);
    
    <T extends XmlObject> XmlEntity<T> release(XmlEntity<T> theEntity, String password, Class<T> expectedType);
    
    /**
     * Determine whether the entity is encrypted or not
     * @param xmlEntityId
     * @return
     */
    boolean isEncrypted(UUID xmlEntityId);
    
    /**
     * Retrieve the specified entity. If the entity is encrypted, an attempt will be made to 
     * open it.
     * @param xmlEntityId
     * @param expectedType
     * @return
     */
    <T extends XmlObject> XmlEntity<T> retrieveEntity(UUID xmlEntityId, Class<T> expectedType);
    
    /**
     * Delete the entity
     * @param xmlEntityId
     */
    void delete(UUID xmlEntityId);
}
