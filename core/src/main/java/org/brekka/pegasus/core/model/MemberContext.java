/**
 *
 */
package org.brekka.pegasus.core.model;

import org.brekka.phalanx.api.model.AuthenticatedPrincipal;
import org.brekka.phalanx.api.model.KeyPair;
import org.brekka.phalanx.api.model.PrivateKeyToken;
import org.brekka.xml.pegasus.v2.model.ProfileType;

public interface MemberContext extends AccessorContextAware {

    Actor getActiveActor();

    void setActiveActor(Actor actor);

    Member getMember();

    Vault getActiveVault();

    ProfileType getProfile();

    Profile getActiveProfile();

    AuthenticatedPrincipal getVaultKey(Vault vault);

    void retainVaultKey(Vault vault);

    void clearVault(Vault vault);

    PrivateKeyToken getPrivateKey(KeyPair keyPair);

    void retainPrivateKey(KeyPair keyPair, PrivateKeyToken privateKeyToken);
}
