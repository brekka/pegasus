/**
 * 
 */
package org.brekka.pegasus.core.model;

/**
 * Possible states that a member can be in
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public enum ActorStatus {
    /**
     * The member has just logged in and needs to setup their profile.
     */
    NEW,
    /**
     * Member is active
     */
    ACTIVE,
    /**
     * The member has been disable by an administrator
     */
    DISABLED,
    /**
     * Marked with this while record awaits permanent deletion
     */
    DELETED,
}
