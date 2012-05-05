/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.UUID;

import org.brekka.xml.pegasus.v1.model.ProfileType;

/**
 * @author Andrew Taylor
 *
 */
public interface AuthenticatedMember {

    
    Member getMember();
    
    OpenVault getActiveVault();
    
    ProfileType getProfile();
    
    OpenVault getVault(UUID vaultId);
}
