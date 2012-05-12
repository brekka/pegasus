/**
 * 
 */
package org.brekka.pegasus.core.model;

import org.brekka.xml.pegasus.v1.model.ProfileType;

/**
 * @author Andrew Taylor
 *
 */
public interface AuthenticatedMember {

    Actor getActiveActor();
    
    Member getMember();
    
    Vault getActiveVault();
    
    ProfileType getProfile();
    
}
