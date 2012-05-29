/**
 * 
 */
package org.brekka.pegasus.core.services;

/**
 * Remove bundles that have passed their expiry date.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface BundleReaperService {

    /**
     * Perform background de-allocation.
     */
    void deallocate();
}
