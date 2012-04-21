/**
 * 
 */
package org.brekka.pegasus.core.model;

import org.brekka.xml.pegasus.v1.model.BundleType;

/**
 * @author Andrew Taylor
 *
 */
public interface UnlockedBundle {

    Bundle getBundleModel();
    
    BundleType getBundleXml();
}
