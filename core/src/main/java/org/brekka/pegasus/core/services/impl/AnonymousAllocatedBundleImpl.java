/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.UUID;

import org.brekka.pegasus.core.services.AnonymousService;

/**
 * @author Andrew Taylor
 *
 */
class AnonymousAllocatedBundleImpl implements AnonymousService.AnonymousAllocatedBundle {

    private final UUID bundleId;
    private final String token;
    private final String code;
    private final String fileName;
    
    public AnonymousAllocatedBundleImpl(UUID bundleId, String token, String code, String fileName) {
        this.bundleId = bundleId;
        this.token = token;
        this.code = code;
        this.fileName = fileName;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.AllocatedBundle#getBundleId()
     */
    @Override
    public UUID getBundleId() {
        return bundleId;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.AllocatedBundle#getToken()
     */
    @Override
    public String getToken() {
        return token;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.AllocatedBundle#getCode()
     */
    @Override
    public String getCode() {
        return code;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.AllocatedBundle#getFileName()
     */
    @Override
    public String getFileName() {
        return fileName;
    }

}
