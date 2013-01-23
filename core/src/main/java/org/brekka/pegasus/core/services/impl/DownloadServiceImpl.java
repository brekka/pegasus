/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.springframework.transaction.annotation.Transactional;

/**
 * Provide the ability to download with events.
 *
 * @author Andrew Taylor (andrew@brekka.org)
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
    @Transactional()
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
