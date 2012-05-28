/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import javax.crypto.SecretKey;

import org.brekka.pegasus.core.model.Bundle;
import org.brekka.pegasus.core.services.AnonymousService;

/**
 * @author Andrew Taylor
 *
 */
class AnonymousAllocatedBundleImpl extends AbstractAllocatedBundle implements AnonymousService.AnonymousAllocatedBundle {

    
    private final String token;
    private final String code;
    private final String fileName;
    
    public AnonymousAllocatedBundleImpl(Bundle bundle, SecretKey secretKey, String token, String code, String fileName) {
        super(bundle, secretKey);
        this.token = token;
        this.code = code;
        this.fileName = fileName;
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
