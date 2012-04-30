/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import org.brekka.pegasus.core.model.TransferKey;

/**
 * @author Andrew Taylor
 *
 */
public class TransferKeyImpl implements TransferKey {

    private final String token;
    private final String code;
    private final String fileName;
    
    
    
    public TransferKeyImpl(String token, String code, String fileName) {
        this.token = token;
        this.code = code;
        this.fileName = fileName;
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
