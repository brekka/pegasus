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
    <T extends XmlObject> XmlEntity<T> persistPlainEntity(T xml);
    
    /**
     * Store the specified XML in the database, making sure to encrypt it and protect the key with
     * the specified cryptoStore.
     * @param xml
     * @param keySafe
     * @return
     */
    <T extends XmlObject> XmlEntity<T> persistEncryptedEntity(T xml, KeySafe keySafe);
    
    <T extends XmlObject> XmlEntity<T> updateEntity(XmlEntity<T> updated, XmlEntity<T> lockedCurrent, Class<T> xmlType);
    
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
