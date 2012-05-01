/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.core.model.InboxTransferKey;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
class InboxTransferKeyImp implements InboxTransferKey {

    private final Inbox inbox;
    
    private final int fileCount;
    
    
    public InboxTransferKeyImp(Inbox inbox, int fileCount) {
        this.inbox = inbox;
        this.fileCount = fileCount;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.InboxTransferKey#getFileCount()
     */
    @Override
    public int getFileCount() {
        return fileCount;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.InboxTransferKey#getInbox()
     */
    @Override
    public Inbox getInbox() {
        return inbox;
    }

}
