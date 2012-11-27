/**
 * 
 */
package org.brekka.pegasus.web.support;

import org.brekka.stillingar.api.annotations.Configured;
import org.brekka.xml.pegasus.v2.config.PegasusDocument.Pegasus;

/**
 * @author Andrew Taylor
 *
 */
@Configured
public class Configuration {

    @Configured
    private Pegasus pegasus;
    
    
    public String getFetchBase() {
        return pegasus.getFetchBase();
    }
    
    
    /**
     * @return the pegasus
     */
    public Pegasus getRoot() {
        return pegasus;
    }
    
}
