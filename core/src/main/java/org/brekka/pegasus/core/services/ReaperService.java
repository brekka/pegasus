/**
 * 
 */
package org.brekka.pegasus.core.services;

/**
 * Remove bundles that have passed their expiry date.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface ReaperService {

    /**
     * Perform background de-allocation of bundles
     */
    void deallocateBundles();
    
    /**
     * Perform background de-allocation of bundle files
     */
    void deallocateBundleFiles();
}
