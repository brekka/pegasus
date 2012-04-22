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
import org.brekka.pegasus.core.model.FileDownloadEvent;
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
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.DownloadService#download(org.brekka.xml.pegasus.v1.model.FileType, java.lang.String, java.lang.String, java.lang.String, java.io.OutputStream)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public InputStream download(FileType fileType) {
        UUID fileId = UUID.fromString(fileType.getUUID());
        FileDownloadEvent event = eventService.beginFileDownloadEvent(fileId);
        CryptedFile cryptedFile = pavewayService.retrieveCryptedFileById(fileId);
        CryptoFactory cryptoFactory = cryptoFactoryRegistry.getFactory(cryptedFile.getProfile());
        SecretKey secretKey = new SecretKeySpec(fileType.getKey(), cryptoFactory.getSymmetric().getKeyGenerator().getAlgorithm());
        InputStream is = pavewayService.download(cryptedFile, secretKey);
        return new EventInputStream(is, event, cryptedFile.getOriginalLength());
    }
    
    private class EventInputStream extends FilterInputStream {

        private final FileDownloadEvent event;
        
        private final long expectedLength;
        
        private long length;
        
        public EventInputStream(InputStream in, FileDownloadEvent event, long expectedLength) {
            super(in);
            this.event = event;
            this.expectedLength = expectedLength;
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
            }
        }
    }
    
}
