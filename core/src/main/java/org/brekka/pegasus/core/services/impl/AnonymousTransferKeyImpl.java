/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.UUID;

import org.brekka.pegasus.core.model.AnonymousTransferKey;

/**
 * @author Andrew Taylor
 *
 */
public class AnonymousTransferKeyImpl implements AnonymousTransferKey {

    private final UUID bundleId;
    private final String token;
    private final String code;
    private final String fileName;
    
    public AnonymousTransferKeyImpl(UUID bundleId, String token, String code, String fileName) {
        this.bundleId = bundleId;
        this.token = token;
        this.code = code;
        this.fileName = fileName;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.TransferKey#getBundleId()
     */
    @Override
    public UUID getBundleId() {
        return bundleId;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.TransferKey#getToken()
     */
    @Override
    public String getToken() {
        return token;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.TransferKey#getCode()
     */
    @Override
    public String getCode() {
        return code;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.TransferKey#getFileName()
     */
    @Override
    public String getFileName() {
        return fileName;
    }

}
