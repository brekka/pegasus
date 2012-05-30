/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.io.InputStream;

import org.brekka.pegasus.core.model.BundleFile;

/**
 * @author Andrew Taylor
 *
 */
public interface DownloadService {

    InputStream download(BundleFile file);
}
