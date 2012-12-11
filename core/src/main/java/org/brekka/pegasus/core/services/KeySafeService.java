/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.util.UUID;

import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Partnership;
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

    /**
     * @param organization
     * @param clientsDivision
     * @param rootDivision
     */
    <Owner extends Actor> Partnership<Owner> createPartnership(Owner owner, Division<Owner> source, Division<?> target);
}
