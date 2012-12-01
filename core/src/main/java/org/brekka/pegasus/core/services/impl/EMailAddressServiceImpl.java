/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.nio.charset.Charset;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.brekka.pegasus.core.dao.DomainNameDAO;
import org.brekka.pegasus.core.dao.EMailAddressDAO;
import org.brekka.pegasus.core.model.DomainName;
import org.brekka.pegasus.core.model.EMailAddress;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.services.EMailAddressService;
import org.brekka.phoenix.api.CryptoProfile;
import org.brekka.phoenix.api.DerivedKey;
import org.brekka.phoenix.api.services.DerivedKeyCryptoService;
import org.brekka.stillingar.api.annotations.Configured;
import org.brekka.xml.pegasus.v2.config.EMailAddressServiceDocument;
import org.brekka.xml.pegasus.v2.config.SystemDerivedKeySpecType;
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
    
    @Autowired
    private DerivedKeyCryptoService derivedKeyCryptoService;
    
    /**
     * Will be combined with all e-mail hashes. Ensures that an attacked with access to the database will not be
     * able to identify addresses without access to this salt.
     */
    @Configured
    private EMailAddressServiceDocument.EMailAddressService config;
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.EMailAddressService#createEMail(java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public EMailAddress createEMail(String email, Member owner, boolean requiresVerification) {
        String domain = StringUtils.substringAfterLast(email, "@");
        
        DomainName domainName = toDomainName(domain);
        
        EMailAddress eMailAddress = new EMailAddress();
        eMailAddress.setHash(hash(email));
        eMailAddress.setDomainName(domainName);
        eMailAddress.setAddress(email);
        eMailAddress.setOwner(owner);
        if (!requiresVerification) {
            eMailAddress.setActive(Boolean.TRUE);
            eMailAddress.setVerified(new Date());
        } else {
            // TODO
//            eMailAddress.setVerificationCode("");
        }
        eMailAddressDAO.create(eMailAddress);
        
        // TODO add to profile
        return eMailAddress;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.EMailAddressService#retrieveByAddress(java.lang.String)
     */
    @Override
    public EMailAddress retrieveByAddress(String address) {
        byte[] hash = hash(address);
        return eMailAddressDAO.retrieveByHash(hash);
    }

    /**
     * @param domain
     * @return
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public DomainName toDomainName(String domain) {
        byte[] hash = hash(domain);
        DomainName domainName = domainNameDAO.retrieveByHash(hash);
        if (domainName == null) {
            domainName = new DomainName();
            domainName.setHash(hash);
            domainName.setAddress(domain);
            domainNameDAO.create(domainName);
        }
        return domainName;
    }

    protected byte[] hash(String value) {
        SystemDerivedKeySpecType spec = config.getSystemDerivedKeySpec();
        byte[] data = value.getBytes(Charset.forName("UTF-8"));
        DerivedKey derivedKey = derivedKeyCryptoService.apply(data, spec.getSalt(), spec.getIterations(), CryptoProfile.Static.of(spec.getCryptoProfile()));
        return derivedKey.getDerivedKey();
    }
}
