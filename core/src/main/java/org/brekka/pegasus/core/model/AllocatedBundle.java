/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.UUID;

/**
 * Marker interface used to abstractly identify a recently created bundle
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface AllocatedBundle {
    
    UUID getBundleId();
    
    boolean isAgreement();
}
