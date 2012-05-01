/**
 * 
 */
package org.brekka.pegasus.core.model;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface InboxTransferKey extends TransferKey {
    int getFileCount();
    
    Inbox getInbox();
}
