/**
 * Copyright (c) 2015 Digital Shadows Ltd.
 */

package org.brekka.pegasus.core.security;

import org.springframework.security.core.Authentication;

/**
 * Should be applied to the principal returned by {@link Authentication#getPrincipal()} to identify the Pegasus user currently active.
 */
public interface PegasusPrincipalAware {

    /**
     * Retrieve the Pegasus principal
     *
     * @return the principal
     */
    PegasusPrincipal getPegasusPrincipal();
}
