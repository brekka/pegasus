/**
 * 
 */
package org.brekka.pegasus.web.support;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public final class MakeKeyUtils {

    /**
     * 
     */
    private MakeKeyUtils() {
    }
    
    public static String newKey() {
        return RandomStringUtils.randomAlphabetic(4);
    }
}
