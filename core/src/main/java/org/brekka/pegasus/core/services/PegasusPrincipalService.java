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

import java.util.function.Supplier;

import org.brekka.pegasus.core.model.AuthenticationToken;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.security.PegasusPrincipal;
import org.brekka.pegasus.core.security.PegasusPrincipalAware;
import org.brekka.phalanx.api.model.ExportedPrincipal;

public interface PegasusPrincipalService {

    PegasusPrincipal currentPrincipal(boolean required);

    PegasusPrincipal login(Member member, Organization organization, Vault vault, String vaultPassword);

    PegasusPrincipal login(Member member, Organization organization, String vaultPassword);

    ExportedPrincipal exportPrincipal(PegasusPrincipal principal, byte[] restoreSecret);

    PegasusPrincipal restorePrincipal(Member member, Organization organization, ExportedPrincipal restore,
            byte[] restoreSecret);

    void doWithPrincipal(PegasusPrincipal principal, Runnable runnable);

    <T> T doWithPrincipal(PegasusPrincipal principal, Supplier<T> supplier);

    void logout(PegasusPrincipal principal);

    @Deprecated
    PegasusPrincipal principal(AuthenticationToken token);

    @Deprecated
    void loginAndBind(PegasusPrincipalAware principalSource, String vaultPassword, Organization organization,
            boolean restoreRequired);

    @Deprecated
    void loginAndBind(PegasusPrincipalAware principalSource, String password, Organization organization,
            Member member, Vault vault);

    @Deprecated
    void unbind();

    @Deprecated
    void restore(PegasusPrincipal principal, String password);

    @Deprecated
    boolean restore(PegasusPrincipal principal, byte[] secret);


}
