/**
 * 
 */
package org.brekka.pegasus.core.model;

/**
 * @author Andrew Taylor
 *
 */
public interface AuthenticatedMember {

    
    Member getMember();
    
    OpenVault getActiveVault();
}
