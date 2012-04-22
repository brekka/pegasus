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

    private final String slug;
    private final String code;
    private final String fileName;
    
    
    
    public TransferKeyImpl(String slug, String code, String fileName) {
        this.slug = slug;
        this.code = code;
        this.fileName = fileName;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.TransferKey#getSlug()
     */
    @Override
    public String getSlug() {
        return slug;
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
