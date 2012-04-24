/**
 * 
 */
package org.brekka.pegasus.core.services;

import org.brekka.pegasus.core.model.AuthenticatedMember;

/**
 * @author Andrew Taylor
 *
 */
public interface MemberService {

    /**
     * @return
     */
    boolean isNewMember();

    void setupMember(String name, String email, String vaultPassword);
    
    AuthenticatedMember getCurrent();
}
