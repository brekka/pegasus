/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.io.InputStream;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.brekka.paveway.core.model.CryptedFile;
import org.brekka.paveway.core.services.PavewayService;
import org.brekka.pegasus.core.services.DownloadService;
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
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.DownloadService#download(org.brekka.xml.pegasus.v1.model.FileType, java.lang.String, java.lang.String, java.lang.String, java.io.OutputStream)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public InputStream download(FileType fileType, String remoteIP, String onBehalfOfAddress, String userAgent) {
        UUID fileId = UUID.fromString(fileType.getUUID());
        CryptedFile cryptedFile = pavewayService.retrieveCryptedFileById(fileId);
        CryptoFactory cryptoFactory = cryptoFactoryRegistry.getFactory(cryptedFile.getProfile());
        SecretKey secretKey = new SecretKeySpec(fileType.getKey(), cryptoFactory.getSymmetric().getKeyGenerator().getAlgorithm());
        return pavewayService.download(cryptedFile, secretKey);
    }
}
