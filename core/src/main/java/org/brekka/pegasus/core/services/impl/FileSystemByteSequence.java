/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brekka.paveway.core.model.ByteSequence;
import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.joda.time.LocalDate;

/**
 * @author Andrew Taylor
 *
 */
public class FileSystemByteSequence implements ByteSequence {
    
    private static final Log log = LogFactory.getLog(FileSystemByteSequence.class);

    private final UUID id;
    
    private final File file;
    
    
    public FileSystemByteSequence(UUID id, File file, boolean create) {
        this.id = id;
        this.file = file;
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.ByteSequence#getId()
     */
    @Override
    public UUID getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.ByteSequence#getOutputStream()
     */
    @Override
    public OutputStream getOutputStream() {
        try {
            return new FileOutputStream(file) { 
                @Override
                public void close() throws IOException {
                    super.close();
                    try {
                        // Reduce information leakage by setting modified timestamp to the beginning of the day
                        // The reason for not setting to the epoch is so that incremental backups can still be performed.
                        file.setLastModified(LocalDate.now().toDate().getTime());
                    } catch (Exception e) {
                        if (log.isDebugEnabled()) {
                            log.debug(String.format("Failed to reset last modified for '%s'", file), e);
                        }
                    }
                }
            };
        } catch (FileNotFoundException e) {
            throw new PegasusException(PegasusErrorCode.PG100, 
                    "Failed to copy data for resource id '%s'", id);
        }
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.ByteSequence#getInputStream()
     */
    @Override
    public InputStream getInputStream() {
        try {
            return new FileInputStream(file);
        } catch (IOException e) {
            throw new PegasusException(PegasusErrorCode.PG101, 
                    "Failed to read data for resource id '%s'", id);
        }
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.ByteSequence#persist()
     */
    @Override
    public void persist() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.ByteSequence#discard()
     */
    @Override
    public void discard() {
        // TODO Auto-generated method stub

    }

}
