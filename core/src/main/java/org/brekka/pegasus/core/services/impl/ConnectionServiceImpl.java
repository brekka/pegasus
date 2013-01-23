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

import static org.brekka.pegasus.core.utils.PegasusUtils.asType;
import static org.brekka.pegasus.core.utils.PegasusUtils.checkNotNull;

import java.util.UUID;

import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.dao.ConnectionDAO;
import org.brekka.pegasus.core.event.VaultDeleteEvent;
import org.brekka.pegasus.core.model.Connection;
import org.brekka.pegasus.core.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides functionality to manipulate {@link Connection} instances.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Service
@Transactional
public class ConnectionServiceImpl implements ConnectionService, ApplicationListener<ApplicationEvent> {

    @Autowired
    private ConnectionDAO connectionDAO;
    
    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    @Override
    @Transactional()
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof VaultDeleteEvent) {
            VaultDeleteEvent vaultDeleteEvent = (VaultDeleteEvent) event;
            connectionDAO.deleteWithSourceKeySafe(vaultDeleteEvent.getVault());
        }
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.ConnectionService#retrieveById(java.util.UUID, java.lang.Class)
     */
    @Override
    @Transactional(readOnly=true)
    public <T extends Connection<?, ?, ?>> T retrieveById(UUID connectionId, Class<T> expectedType) {
        checkNotNull(connectionId, "connectionId");
        checkNotNull(expectedType, "expectedType");
        Connection<?, ?, ?> connection = connectionDAO.retrieveById(connectionId);
        return asType(connection, expectedType, PegasusErrorCode.PG444, "Connection '%s'", connectionId);
    }
}
