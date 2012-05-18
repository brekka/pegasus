/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import javax.crypto.SecretKey;

import org.brekka.pegasus.core.model.Bundle;
import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.core.services.InboxService;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
class InboxAllocatedBundleImpl extends AbstractAllocatedBundle implements InboxService.InboxAllocatedBundle {

    private final Inbox inbox;
    private final int fileCount;
    
    
    public InboxAllocatedBundleImpl(Bundle bundle, SecretKey secretKey, Inbox inbox, int fileCount) {
        super(bundle, secretKey);
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
