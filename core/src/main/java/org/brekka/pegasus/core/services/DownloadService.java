/**
 * 
 */
package org.brekka.pegasus.core.services;

import java.io.InputStream;

import org.brekka.xml.pegasus.v1.model.FileType;

/**
 * @author Andrew Taylor
 *
 */
public interface DownloadService {

    InputStream download(FileType fileType);
}
