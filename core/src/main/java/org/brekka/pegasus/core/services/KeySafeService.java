/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.UUID;

import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.phalanx.api.model.CryptedData;

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
}
