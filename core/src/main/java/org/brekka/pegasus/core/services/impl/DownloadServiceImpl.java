/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.paveway.core.services.PavewayService;
import org.brekka.pegasus.core.model.AllocationFile;
import org.brekka.pegasus.core.model.FileDownloadEvent;
import org.brekka.pegasus.core.model.Transfer;
import org.brekka.pegasus.core.services.AllocationService;
import org.brekka.pegasus.core.services.DownloadService;
import org.brekka.pegasus.core.services.EventService;
import org.brekka.phoenix.CryptoFactory;
import org.brekka.phoenix.CryptoFactoryRegistry;
import org.brekka.xml.pegasus.v1.model.FileType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Andrew Taylor
 *
 */
@Service
@Transactional
public class DownloadServiceImpl implements DownloadService {

    @Autowired
    private PavewayService pavewayService;
    
    @Autowired
    private CryptoFactoryRegistry cryptoFactoryRegistry;
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private AllocationService allocationService;
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.DownloadService#download(
     *      org.brekka.xml.pegasus.v1.model.FileType, java.lang.String, 
     *      java.lang.String, java.lang.String, java.io.OutputStream)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public InputStream download(AllocationFile file, Transfer transfer, ProgressCallback progressCallback) {
        FileType fileType = file.getXml();
        UUID fileId = UUID.fromString(fileType.getUUID());
        FileDownloadEvent event = eventService.beginFileDownloadEvent(file);
        CryptedFile cryptedFile = pavewayService.retrieveCryptedFileById(fileId);
        CryptoFactory cryptoFactory = cryptoFactoryRegistry.getFactory(cryptedFile.getProfile());
        SecretKey secretKey = new SecretKeySpec(fileType.getKey(), 
                cryptoFactory.getSymmetric().getKeyGenerator().getAlgorithm());
        InputStream is = pavewayService.download(cryptedFile, secretKey);
        return new EventInputStream(is, file, event, cryptedFile.getOriginalLength(), progressCallback);
    }
    
    private class EventInputStream extends FilterInputStream {

        private final AllocationFile allocationFile;
        
        private final FileDownloadEvent event;
        
        private final long expectedLength;
        
        private final ProgressCallback progressCallback;
        
        private long length;
        
        
        public EventInputStream(InputStream in, AllocationFile allocationFile, FileDownloadEvent event, 
                long expectedLength, ProgressCallback progressCallback) {
            super(in);
            this.allocationFile = allocationFile;
            this.event = event;
            this.expectedLength = expectedLength;
            this.progressCallback = progressCallback;
        }
        
        /* (non-Javadoc)
         * @see java.io.FilterInputStream#read(byte[], int, int)
         */
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int i = super.read(b, off, len);
            if (i != -1) {
                length += i;
            }
            if (progressCallback != null) {
                progressCallback.update(length, expectedLength);
            }
            return i;
        }
        
        /* (non-Javadoc)
         * @see java.io.FilterInputStream#close()
         */
        @Override
        public void close() throws IOException {
            super.close();
            if (expectedLength == length) {
                eventService.completeEvent(event);
                allocationService.incrementDownloadCounter(allocationFile);
                if (progressCallback != null) {
                    progressCallback.update(length, expectedLength);
                }
            }
        }
    }
    
}
