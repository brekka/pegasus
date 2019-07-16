/*
 * Copyright 2016 the original author or authors.
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

package org.brekka.pegasus.core.services;

import org.brekka.pegasus.core.model.AuthenticationToken;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.security.PegasusPrincipal;
import org.brekka.pegasus.core.security.PegasusPrincipalAware;

/**
 *
 */
public interface PegasusPrincipalService {

    PegasusPrincipal currentPrincipal(boolean required);

    PegasusPrincipal principal(AuthenticationToken token);

    void loginAndBind(PegasusPrincipalAware principalSource, String vaultPassword, Organization organization,
            boolean restoreRequired);

    void loginAndBind(PegasusPrincipalAware principalSource, String password, Organization organization,
            Member member, Vault vault);

    void logout(PegasusPrincipal principal);

    void unbind();

    void restore(PegasusPrincipal principal, String password);

    boolean restore(PegasusPrincipal principal, byte[] secret);

    void doWithPrincipal(PegasusPrincipal principal, Runnable runnable);
}
