/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.paveway.core.services.PavewayService;
import org.brekka.pegasus.core.model.AllocationFile;
import org.brekka.pegasus.core.model.FileDownloadEvent;
import org.brekka.pegasus.core.services.AllocationService;
import org.brekka.pegasus.core.services.DownloadService;
import org.brekka.pegasus.core.services.EventService;
import org.brekka.phoenix.api.CryptoProfile;
import org.brekka.phoenix.api.SecretKey;
import org.brekka.phoenix.api.services.CryptoProfileService;
import org.brekka.phoenix.api.services.SymmetricCryptoService;
import org.brekka.xml.pegasus.v2.model.FileType;
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
    private CryptoProfileService cryptoProfileService;
    
    @Autowired
    private SymmetricCryptoService symmetricCryptoService;
    
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
    public InputStream download(AllocationFile file, ProgressCallback progressCallback) {
        FileType fileType = file.getXml();
        FileDownloadEvent event = eventService.beginFileDownloadEvent(file);
        CryptedFile cryptedFile = pavewayService.retrieveCryptedFileById(file.getCryptedFile().getId());
        CryptoProfile cryptoProfile = cryptoProfileService.retrieveProfile(cryptedFile.getProfile());
        SecretKey secretKey = symmetricCryptoService.toSecretKey(fileType.getKey(), cryptoProfile);
        cryptedFile.setSecretKey(secretKey);
        InputStream is = pavewayService.download(cryptedFile);
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
