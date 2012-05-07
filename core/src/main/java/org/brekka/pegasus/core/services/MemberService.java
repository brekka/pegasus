/**
 * 
 */
package org.brekka.pegasus.core.services;

import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.springframework.security.core.context.SecurityContext;

/**
 * @author Andrew Taylor
 *
 */
public interface MemberService {

    /**
     * @return
     */
    boolean isNewMember();

    void setupMember(String name, String email, String vaultPassword, boolean encryptedProfile);
    
    AuthenticatedMember getCurrent();

    /**
     * @param securityContext
     */
    void logout(SecurityContext securityContext);

}

