/*
 * Copyright 2012 the original author or authors.
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

import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.dao.UsernamePasswordDAO;
import org.brekka.pegasus.core.model.AuthenticationToken;
import org.brekka.pegasus.core.model.UsernamePassword;
import org.brekka.pegasus.core.services.UsernamePasswordService;
import org.brekka.phoenix.api.CryptoProfile;
import org.brekka.phoenix.api.DerivedKey;
import org.brekka.phoenix.api.services.CryptoProfileService;
import org.brekka.phoenix.api.services.DerivedKeyCryptoService;
import org.brekka.phoenix.api.services.DigestCryptoService;
import org.brekka.stillingar.api.annotations.Configured;
import org.brekka.xml.pegasus.v2.config.SystemDerivedKeySpecType;
import org.brekka.xml.pegasus.v2.config.UsernamePasswordServiceDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * UsernamePasswordServiceImpl
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Service
@Transactional
@Configured
public class UsernamePasswordServiceImpl implements UsernamePasswordService {

    @Autowired
    private UsernamePasswordDAO usernamePasswordDAO;
    
    @Autowired
    private DerivedKeyCryptoService derivedKeyCryptoService;
    
    @Autowired
    private DigestCryptoService digestCryptoService;
    
    @Autowired
    private CryptoProfileService cryptoProfileService;
    
    @Configured
    private UsernamePasswordServiceDocument.UsernamePasswordService config;
    
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.UsernamePasswordService#create(java.lang.String, java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public UsernamePassword create(String username, String password) {
        byte[] derivedUsername = deriveUsername(username);
        byte[] passwordBytes = toBytes(password);

        CryptoProfile cryptoProfile = CryptoProfile.DEFAULT;
        DerivedKey derivedKey = derivedKeyCryptoService.apply(passwordBytes, cryptoProfile);
        
        UsernamePassword usernamePassword = new UsernamePassword();
        usernamePassword.setIterations(derivedKey.getIterations());
        usernamePassword.setPassword(derivedKey.getDerivedKey());
        usernamePassword.setProfile(derivedKey.getCryptoProfile().getNumber());
        usernamePassword.setSalt(derivedKey.getSalt());
        usernamePassword.setUsernameDigest(derivedUsername);
        usernamePassword.setUsername(username);
        usernamePasswordDAO.create(usernamePassword);
        return usernamePassword;
    }
    

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.UsernamePasswordService#verify(java.lang.String, java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.SUPPORTS)
    public boolean verify(UsernamePassword usernamePassword, String password) {
        byte[] passwordBytes = toBytes(password);
        return derivedKeyCryptoService.check(passwordBytes, usernamePassword);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.UsernamePasswordService#retrieveByUsername(java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.SUPPORTS)
    public UsernamePassword retrieveByUsername(String username) {
        byte[] derivedUsername = deriveUsername(username);
        UsernamePassword usernamePassword = usernamePasswordDAO.retrieveByUsernameDigest(derivedUsername);
        if (usernamePassword != null) {
            usernamePassword.setUsername(username);
        }
        return usernamePassword;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.UsernamePasswordService#changePassword(org.brekka.pegasus.core.model.UsernamePassword, java.lang.String, java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void changePassword(UsernamePassword usernamePassword, String oldPassword, String newPassword) {
        if (verify(usernamePassword, oldPassword)) {
            internalChangePassword(usernamePassword, newPassword);
        } else {
            throw new PegasusException(PegasusErrorCode.PG103, 
                    "Unable to change password for user '%s', old password is incorrect", usernamePassword.getUsername());
        }
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.UsernamePasswordService#changePassword(org.brekka.pegasus.core.model.UsernamePassword, java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void changePassword(UsernamePassword usernamePassword, String newPassword) {
        internalChangePassword(usernamePassword, newPassword);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.UsernamePasswordService#delete(org.brekka.pegasus.core.model.AuthenticationToken)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void delete(AuthenticationToken authenticationToken) {
        usernamePasswordDAO.delete(authenticationToken.getId());
    }
    
    protected void internalChangePassword(UsernamePassword usernamePassword, String newPassword) {
        byte[] passwordBytes = toBytes(newPassword);
        DerivedKey derivedKey = derivedKeyCryptoService.apply(passwordBytes, CryptoProfile.DEFAULT);
        usernamePassword.setIterations(derivedKey.getIterations());
        usernamePassword.setPassword(derivedKey.getDerivedKey());
        usernamePassword.setProfile(derivedKey.getCryptoProfile().getNumber());
        usernamePassword.setSalt(derivedKey.getSalt());
        usernamePasswordDAO.update(usernamePassword);
    }
    
    protected byte[] deriveUsername(String username) {
        SystemDerivedKeySpecType spec = config.getSystemDerivedKeySpec();
        if (!config.getUserNameCaseSensitive()) {
            username = username.toUpperCase();
        }
        byte[] data = toBytes(username);
        DerivedKey derivedKey = derivedKeyCryptoService.apply(data, spec.getSalt(), null, CryptoProfile.Static.of(spec.getCryptoProfile()));
        return derivedKey.getDerivedKey();
    }

    protected byte[] toBytes(String text) {
        return text.getBytes(Charset.forName("UTF-8"));
    }
}
