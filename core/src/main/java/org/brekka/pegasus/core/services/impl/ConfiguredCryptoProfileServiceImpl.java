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

import org.brekka.phoenix.api.CryptoProfile;
import org.brekka.phoenix.api.services.CryptoProfileService;
import org.brekka.phoenix.core.services.impl.CryptoProfileServiceImpl;
import org.brekka.stillingar.api.annotations.ConfigurationListener;
import org.brekka.stillingar.api.annotations.Configured;
import org.brekka.xml.phoenix.v2.model.CryptoProfileRegistryDocument.CryptoProfileRegistry;

/**
 * A very simple implementation of {@link CryptoProfileService} that retrieves the profile
 * from the Pegasus configuration file.
 * 
 * TODO when the central authority becomes available, use that.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Configured
public class ConfiguredCryptoProfileServiceImpl implements CryptoProfileService {
    
    private CryptoProfileService delegate;
    
    /**
     * @param profileNumber
     * @return
     * @see org.brekka.phoenix.api.services.CryptoProfileService#retrieveProfile(int)
     */
    public CryptoProfile retrieveProfile(int profileNumber) {
        return delegate.retrieveProfile(profileNumber);
    }

    /**
     * @return
     * @see org.brekka.phoenix.api.services.CryptoProfileService#retrieveDefault()
     */
    public CryptoProfile retrieveDefault() {
        return delegate.retrieveDefault();
    }


    @ConfigurationListener
    public void configure(@Configured 
                             CryptoProfileRegistry cryptoProfileRegistry) {
        delegate = new CryptoProfileServiceImpl(cryptoProfileRegistry);
    }
}
