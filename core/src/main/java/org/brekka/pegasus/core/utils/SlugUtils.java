/**
 * 
 */
package org.brekka.pegasus.core.utils;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
public final class SlugUtils {

    /**
     * Utility non-constructor
     */
    private SlugUtils() {
    }
    
    public static final String sluggify(final String value) {
        String name = value.replaceAll("\\W+", "_");
        name = name.toLowerCase();
        return name;
    }
}
