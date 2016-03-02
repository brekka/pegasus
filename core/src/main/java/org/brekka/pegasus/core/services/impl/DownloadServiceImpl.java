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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Provide the ability to download with events.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Service
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

    @Autowired
    private PlatformTransactionManager transactionManager;



    @Override
    public InputStream download(final AllocationFile file, final ProgressCallback progressCallback) {
        return download(file, progressCallback, true, true);
    }

    @Override
    public InputStream download(final AllocationFile file, final ProgressCallback progressCallback,
            final boolean captureDownloadEvent, final boolean incrementCounter) {
        // Has its own transaction
        final FileDownloadEvent event = captureDownloadEvent ? eventService.beginFileDownloadEvent(file) : null;

        // Perform in its own readonly transaction. This ensures that the thread is only using one connection at a time.
        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        tt.setReadOnly(true);
        return tt.execute(new TransactionCallback<EventInputStream>() {
            @Override
            public EventInputStream doInTransaction(final TransactionStatus status) {
                FileType fileType = file.getXml();
                CryptedFile cryptedFile = pavewayService.retrieveCryptedFileById(file.getCryptedFile().getId());
                CryptoProfile cryptoProfile = cryptoProfileService.retrieveProfile(cryptedFile.getProfile());
                SecretKey secretKey = symmetricCryptoService.toSecretKey(fileType.getKey(), cryptoProfile);
                cryptedFile.setSecretKey(secretKey);
                InputStream is = pavewayService.download(cryptedFile);
                return new EventInputStream(is, file, event, cryptedFile.getOriginalLength(), progressCallback, incrementCounter);
            }
        });
    }

    private class EventInputStream extends FilterInputStream {

        private final AllocationFile allocationFile;

        private final FileDownloadEvent event;

        private final long expectedLength;

        private final ProgressCallback progressCallback;

        private long length;

        private boolean incrementCounter;


        public EventInputStream(final InputStream in, final AllocationFile allocationFile, final FileDownloadEvent event,
                final long expectedLength, final ProgressCallback progressCallback, final boolean incrementCounter) {
            super(in);
            this.allocationFile = allocationFile;
            this.event = event;
            this.expectedLength = expectedLength;
            this.progressCallback = progressCallback;
            this.incrementCounter = incrementCounter;
        }

        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            int i = super.read(b, off, len);
            if (i != -1) {
                length += i;
            }
            if (progressCallback != null) {
                progressCallback.update(length, expectedLength);
            }
            return i;
        }

        @Override
        public void close() throws IOException {
            super.close();
            if (expectedLength == length) {
                if (event != null) {
                    eventService.completeEvent(event);
                }
                if (incrementCounter) {
                    allocationService.incrementDownloadCounter(allocationFile);
                }
                if (progressCallback != null) {
                    progressCallback.update(length, expectedLength);
                }
            }
        }
    }
}
