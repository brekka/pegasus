/**
 * 
 */
package org.brekka.pegasus.web.support;

import org.brekka.stillingar.annotations.Configured;
import org.brekka.xml.pegasus.v1.config.PegasusDocument.Pegasus;

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
}
