/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.StringUtils;
import org.brekka.pegasus.core.dao.DomainNameDAO;
import org.brekka.pegasus.core.dao.EMailAddressDAO;
import org.brekka.pegasus.core.model.DomainName;
import org.brekka.pegasus.core.model.EMailAddress;
import org.brekka.pegasus.core.services.EMailAddressService;
import org.brekka.stillingar.annotations.Configured;
import org.brekka.xml.pegasus.v1.config.PegasusDocument.Pegasus.EMailAddresses.Hashing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Service
@Transactional
@Configured
public class EMailAddressServiceImpl implements EMailAddressService {

    @Autowired
    private EMailAddressDAO eMailAddressDAO;
    
    @Autowired
    private DomainNameDAO domainNameDAO;
    
    /**
     * Will be combined with all e-mail hashes. Ensures that an attacked with access to the database will not be
     * able to identify addresses without access to this salt.
     */
    @Configured("//EMailAddresses/Hashing")
    private Hashing hashingConfig;
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.EMailAddressService#createEMail(java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public EMailAddress createEMail(String email) {
        String domain = StringUtils.substringAfterLast(email, "@");
        
        DomainName domainName = toDomainName(domain);
        
        EMailAddress eMailAddress = new EMailAddress();
        eMailAddress.setHash(hash(email));
        eMailAddress.setDomainName(domainName);
        eMailAddress.setAddress(email);
        eMailAddressDAO.create(eMailAddress);
        
        // TODO add to profile
        return eMailAddress;
    }

    /**
     * @param domain
     * @return
     */
    private DomainName toDomainName(String domain) {
        byte[] hash = hash(domain);
        
        // TODO Auto-generated method stub
        return null;
    }

    protected byte[] hash(String value) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(hashingConfig.getAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        byte[] data = value.getBytes(Charset.forName("UTF-8"));
        messageDigest.update(data);
        messageDigest.update(hashingConfig.getSalt());
        for (int i = 1; i < hashingConfig.getIterations(); i++) {
            data = messageDigest.digest(data);
        }
        return data;
    }
}
