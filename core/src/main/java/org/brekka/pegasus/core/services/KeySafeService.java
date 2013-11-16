/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.UUID;

import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.phalanx.api.model.CryptedData;
import org.brekka.phalanx.api.model.KeyPair;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface KeySafeService {

    /**
     * Protect the specified keyData in <code>keySafe</code>
     * @param data
     * @param keySafe
     * @return
     */
    CryptedData protect(byte[] keyData, KeySafe<?> keySafe);

    /**
     * Release the key identified by <code>cryptedDataId</code> which is protected
     * by <code>keySage</code>.
     * 
     * @param cryptedDataId
     * @param keySafe
     * @return
     */
    byte[] release(UUID cryptedDataId, KeySafe<?> keySafe);

    /**
     * @param protectWith
     * @return
     */
    KeyPair createKeyPair(KeySafe<?> protectWith);

    /**
     * @param assignToKeySafe
     * @param keyPairToAssign
     * @return
     */
    KeyPair assignKeyPair(KeySafe<?> protectingKeySafe, KeyPair keyPairToAssign, KeySafe<?> assignToKeySafe);

    /**
     * @param id
     * @return
     */
    KeySafe<? extends Actor> retrieveById(UUID id);

}
