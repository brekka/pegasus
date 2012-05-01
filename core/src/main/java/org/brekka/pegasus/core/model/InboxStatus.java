/**
 * 
 */
package org.brekka.pegasus.core.model;

/**
 * Enumerates the states of an inbox
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public enum InboxStatus {
    /**
     * Fully functional.
     */
    ACTIVE,
    /**
     * Visible to the user that owns it, but functionally inoperable
     */
    DISABLED,
    /**
     * Marked with this while it awaits permanent deletion
     */
    DELETED,
}
