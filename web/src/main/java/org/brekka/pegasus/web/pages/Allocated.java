/**
 * 
 */
package org.brekka.pegasus.web.pages;

import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.model.TransferKey;
import org.brekka.pegasus.web.support.Configuration;

/**
 * @author Andrew Taylor
 *
 */
public class Allocated {

    @Persist(PersistenceConstants.FLASH)
    @Property
    private TransferKey transferKey;
    
    @Inject
    private Configuration configuration;
    
    
    
    void init(TransferKey transferKey) {
        this.transferKey = transferKey;
    }
    
    public String getUnlockLink() {
        return configuration.getFetchBase() + "/" + transferKey.getSlug(); 
    }
    
    public String getDirectLink() {
        String path;
        if (transferKey.getFileName() != null) {
            path = transferKey.getCode() + "/" + transferKey.getSlug() + "/" + transferKey.getFileName();
        } else {
            path = transferKey.getCode() + "/" + transferKey.getSlug() + ".zip";
        }
        return configuration.getFetchBase() + "/" + path;
    }
}
