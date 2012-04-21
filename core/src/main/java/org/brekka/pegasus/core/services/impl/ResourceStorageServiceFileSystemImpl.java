/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.brekka.paveway.core.services.ResourceStorageService;
import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.stillingar.annotations.Configured;
import org.springframework.stereotype.Service;

/**
 * Simple filesystem based resource store
 * 
 * @author Andrew Taylor
 */
@Configured
@Service
public class ResourceStorageServiceFileSystemImpl implements ResourceStorageService {
    
    /**
     * Base location
     */
    @Configured("//c:ResourceStoreDir")
    private URI root;

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.services.ResourceStorageService#store(java.util.UUID, java.io.InputStream)
     */
    @Override
    public OutputStream store(UUID id) {
        File file = toFile(id);
        try {
            return new FileOutputStream(file);
        } catch (IOException e) {
            throw new PegasusException(PegasusErrorCode.PG100, 
                    "Failed to copy data for resource id '%s'", id);
        }
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.services.ResourceStorageService#load(java.util.UUID, java.io.OutputStream)
     */
    @Override
    public InputStream load(UUID id) {
        File file = toFile(id);
        try {
            return new FileInputStream(file);
        } catch (IOException e) {
            throw new PegasusException(PegasusErrorCode.PG101, 
                    "Failed to read data for resource id '%s'", id);
        }
    }

    /* (non-Javadoc)
     * @see org.brekka.paveway.core.services.ResourceStorageService#remove(java.util.UUID)
     */
    @Override
    public void remove(UUID id) {
        File file = toFile(id);
        FileUtils.deleteQuietly(file);
    }
    
    protected File toFile(UUID uuid) {
        String idStr = uuid.toString();
        
//        String part1 = idStr.substring(0, 2);
//        File dir1 = new File(new File(root), part1);
//        if (!dir1.exists()) {
//            dir1.mkdir();
//        }
//        
//        String part2 = idStr.substring(2, 4);
//        File dir2 = new File(dir1, part2);
//        if (!dir2.exists()) {
//            dir2.mkdir();
//        }
//        return new File(dir2, idStr);
        return new File(new File(root), idStr);
    }

}
