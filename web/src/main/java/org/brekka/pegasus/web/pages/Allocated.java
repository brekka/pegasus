/**
 * 
 */
package org.brekka.pegasus.web.pages;

import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.brekka.pegasus.core.model.TransferKey;

/**
 * @author Andrew Taylor
 *
 */
public class Allocated {

    @Persist(PersistenceConstants.FLASH)
    @Property
    private TransferKey transferKey;
    
    
    void init(TransferKey transferKey) {
        this.transferKey = transferKey;
    }
    
    
}
