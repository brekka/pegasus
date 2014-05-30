/**
 *
 */
package org.brekka.pegasus.core.services.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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

    private static final int DEFAULT_BUFFER_SIZE = (1 << 13); // 8096

    private static final Log log = LogFactory.getLog(FileSystemByteSequence.class);

    private final UUID id;

    private final File file;

    private final int bufferSize;


    public FileSystemByteSequence(final UUID id, final File file, final boolean create) {
        this(id, file, create, DEFAULT_BUFFER_SIZE);
    }

    public FileSystemByteSequence(final UUID id, final File file, final boolean create, final int bufferSize) {
        this.id = id;
        this.file = file;
        this.bufferSize = bufferSize;
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.ByteSequence#getId()
     */
    @Override
    public UUID getId() {
        return this.id;
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.ByteSequence#getOutputStream()
     */
    @Override
    public OutputStream getOutputStream() {
        try {
            return new BufferedOutputStream(new FileOutputStream(this.file) {
                @Override
                public void close() throws IOException {
                    super.close();
                    try {
                        // Reduce information leakage by setting modified timestamp to the beginning of the day
                        // The reason for not setting to the epoch is so that incremental backups can still be performed.
                        long stamp = LocalDate.now().toDate().getTime();
                        FileSystemByteSequence.this.file.setLastModified(stamp);
                        // Also clear the parent
                        FileSystemByteSequence.this.file.getParentFile().setLastModified(stamp);
                        // And it's parent
                        File parentFile = FileSystemByteSequence.this.file.getParentFile().getParentFile();
                        if (parentFile.lastModified() > stamp) {
                            // Needs to be amended
                            FileSystemByteSequence.this.file.getParentFile().getParentFile().setLastModified(stamp);
                        }
                    } catch (Exception e) {
                        if (log.isDebugEnabled()) {
                            log.debug(String.format("Failed to reset last modified for '%s'", FileSystemByteSequence.this.file), e);
                        }
                    }
                }
            }, this.bufferSize);
        } catch (FileNotFoundException e) {
            throw new PegasusException(PegasusErrorCode.PG100,
                    "Failed to copy data for resource id '%s'", this.id);
        }
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.ByteSequence#getInputStream()
     */
    @Override
    public InputStream getInputStream() {
        try {
            return new BufferedInputStream(new FileInputStream(this.file), this.bufferSize);
        } catch (IOException e) {
            throw new PegasusException(PegasusErrorCode.PG101,
                    "Failed to read data for resource id '%s'", this.id);
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
