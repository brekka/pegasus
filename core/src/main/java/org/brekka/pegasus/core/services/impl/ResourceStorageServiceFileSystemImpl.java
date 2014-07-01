/**
 *
 */
package org.brekka.pegasus.core.services.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.brekka.paveway.core.model.ByteSequence;
import org.brekka.paveway.core.services.ResourceStorageService;
import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.stillingar.api.annotations.ConfigurationListener;
import org.brekka.stillingar.api.annotations.Configured;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Service;

/**
 * Simple filesystem based resource store
 *
 * @author Andrew Taylor
 */
@Configured
@Service
public class ResourceStorageServiceFileSystemImpl implements ResourceStorageService {


    private static final Log log = LogFactory.getLog(ResourceStorageServiceFileSystemImpl.class);

    private File rootFile;

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.services.ResourceStorageService#allocate(java.util.UUID)
     */
    @Override
    public ByteSequence allocate(final UUID id) {
        File file = toFile(id);
        if (file.exists()) {
            throw new PegasusException(PegasusErrorCode.PG100,
                    "There is already a file with the id '%s'", id);
        }
        return new FileSystemByteSequence(id, file, true);
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.services.ResourceStorageService#retrieve(java.util.UUID)
     */
    @Override
    public ByteSequence retrieve(final UUID id) {
        File file = toFile(id);
        if (!file.exists()) {
            throw new PegasusException(PegasusErrorCode.PG100,
                    "No file found with the id '%s'", id);
        }
        return new FileSystemByteSequence(id, file, false);
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.services.ResourceStorageService#remove(java.util.UUID)
     */
    @Override
    public void remove(final UUID id) {
        File file = toFile(id);
        overwrite(file);
        FileUtils.deleteQuietly(file);
    }

    @ConfigurationListener
    public void configure(
            @Configured("//c:ResourceStoreDir")
            final String pathIn) {
        String path = pathIn;
        if (path.startsWith("file:")) {
            // Remove file prefix
            path = path.substring(5);
        }
        // Allow system properties to be used.
        path = StrSubstitutor.replaceSystemProperties(path);
        this.rootFile = new File(path);
        if (!this.rootFile.exists()) {
            this.rootFile.mkdirs();
        }
        if (log.isInfoEnabled()) {
            log.info(String.format("Initialized resource storage in path '%s'", path));
        }
    }

    protected File toFile(final UUID uuid) {
        String idStr = uuid.toString();

        String part1 = idStr.substring(0, 2);
        File dir1 = new File(this.rootFile, part1);
        if (!dir1.exists()) {
            dir1.mkdir();
            // Reduce information leakage by setting modified timestamp to the beginning of the day
            // The reason for not setting to the epoch is so that incremental backups can still be performed.
            dir1.setLastModified(LocalDate.now().toDate().getTime());
        }

        String part2 = idStr.substring(2, 4);
        File dir2 = new File(dir1, part2);
        if (!dir2.exists()) {
            dir2.mkdir();
            // Reduce information leakage by setting modified timestamp to the beginning of the day
            // The reason for not setting to the epoch is so that incremental backups can still be performed.
            dir2.setLastModified(LocalDate.now().toDate().getTime());
        }
        return new File(dir2, idStr);
    }

    /**
     * Best effort to de-allocate the file with zeros. Seems a waste of resources
     * to write random data, especially given the physical bytes of the original
     * file may not actually get overwritten.
     *
     * @param file
     */
    static void overwrite(final File file) {
        try (RandomAccessFile rwFile = new RandomAccessFile(file, "rw");
               FileChannel rwChannel = rwFile.getChannel()) {
            long size = rwChannel.size();
            MappedByteBuffer buffer = rwChannel.map(FileChannel.MapMode.READ_WRITE, 0, size);
            for (int i = 0; i < size; i++) {
                buffer.put((byte) 0);
            }
            buffer.force();
        } catch (IOException e) {
            throw new PegasusException(PegasusErrorCode.PG100,
                    "Failed to overwrite the file '%s' with zeros", file);
        }
    }
}
