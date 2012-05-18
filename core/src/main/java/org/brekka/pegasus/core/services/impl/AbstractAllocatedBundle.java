/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.UUID;

import javax.crypto.SecretKey;

import org.brekka.pegasus.core.model.AllocatedBundle;
import org.brekka.pegasus.core.model.Bundle;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
class AbstractAllocatedBundle implements AllocatedBundle {
    
    private final Bundle bundle;
    
    private transient SecretKey secretKey;

    public AbstractAllocatedBundle(Bundle bundle, SecretKey secretKey) {
        this.bundle = bundle;
        this.secretKey = secretKey;
    }
    
    /**
     * @return the secretKey
     */
    final SecretKey removeSecretKey() {
        SecretKey secretKey = this.secretKey;
        this.secretKey = null;
        return secretKey;
    }
    
    /**
     * @return the bundle
     */
    final Bundle getBundle() {
        return bundle;
    }


    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.AllocatedBundle#getBundleId()
     */
    @Override
    public final UUID getBundleId() {
        return bundle.getId();
    }
}
