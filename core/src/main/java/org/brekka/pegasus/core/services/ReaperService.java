/**
 * 
 */
package org.brekka.pegasus.core.services;

/**
 * Remove allocations that have passed their expiry date.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface ReaperService {

    /**
     * Perform background de-allocation
     */
    void clearAllocations();
    
    /**
     * Clear the allocation files
     */
    void clearAllocationFiles();
}
