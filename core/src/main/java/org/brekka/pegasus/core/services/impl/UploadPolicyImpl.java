/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import org.brekka.paveway.core.model.UploadPolicy;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
class UploadPolicyImpl implements UploadPolicy {

    private final int maxFiles;
    
    private final int maxFileSize;
    
    private final int maxSize;
    
    private final int clusterSize;
    
    
    
    public UploadPolicyImpl(int maxFiles, int maxFileSize, int maxSize,int clusterSize) {
        this.maxFiles = maxFiles;
        this.maxFileSize = maxFileSize;
        this.maxSize = maxSize;
        this.clusterSize = clusterSize;
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.UploadPolicy#getMaxFiles()
     */
    @Override
    public int getMaxFiles() {
        return maxFiles;
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.UploadPolicy#getMaxFileSize()
     */
    @Override
    public int getMaxFileSize() {
        return maxFileSize;
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.UploadPolicy#getMaxSize()
     */
    @Override
    public int getMaxSize() {
        return maxSize;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.UploadPolicy#getClusterSize()
     */
    @Override
    public int getClusterSize() {
        return clusterSize;
    }

}
