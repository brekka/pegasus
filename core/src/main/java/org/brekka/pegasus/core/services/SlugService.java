/**
 * 
 */
package org.brekka.pegasus.core.services;

import org.brekka.pegasus.core.model.Slug;

/**
 * @author Andrew Taylor
 *
 */
public interface SlugService {

    Slug allocateAnonymous();
}
