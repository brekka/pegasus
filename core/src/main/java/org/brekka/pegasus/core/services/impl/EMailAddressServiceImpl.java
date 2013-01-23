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
 * Stores and provides verification of E-Mail addresses (TODO). The addresses themselves are stored hashed so lookup
 * is based on collisions. As such it is not possible to list all addresses (by design).
 * 
 * @author Andrew Taylor (andrew@brekka.org)
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
        email = email.toLowerCase();
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
    @Transactional(propagation=Propagation.REQUIRED)
    public EMailAddress retrieveByAddress(String address) {
        byte[] hash = hash(address);
        EMailAddress eMailAddress = eMailAddressDAO.retrieveByHash(hash);
        if (eMailAddress == null) {
            return null;
        }
        eMailAddress.setAddress(address);
        return eMailAddress;
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

    /**
     * Generate the hash using the perhaps over-the-top-for-this-purpose SCrypt key derivation function.
     * @param value
     * @return
     */
    protected byte[] hash(String value) {
        value = value.toLowerCase();
        SystemDerivedKeySpecType spec = config.getSystemDerivedKeySpec();
        byte[] data = value.getBytes(Charset.forName("UTF-8"));
        DerivedKey derivedKey = derivedKeyCryptoService.apply(data, spec.getSalt(), null, CryptoProfile.Static.of(spec.getCryptoProfile()));
        return derivedKey.getDerivedKey();
    }
}
