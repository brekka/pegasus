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

package org.brekka.pegasus.core.event;

import org.brekka.pegasus.core.model.Vault;
import org.springframework.context.ApplicationEvent;

/**
 * Event given out during a vault deletion
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class VaultDeleteEvent extends ApplicationEvent {
    /**
     * Serial UID
     */
    private static final long serialVersionUID = -6787564221328973784L;

    public VaultDeleteEvent(Vault vault) {
        super(vault);
    }
    
    public Vault getVault() {
        return (Vault) getSource();
    }
}
