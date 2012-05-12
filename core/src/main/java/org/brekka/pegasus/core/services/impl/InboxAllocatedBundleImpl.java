/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.UUID;

import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.core.services.InboxService;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
class InboxAllocatedBundleImpl implements InboxService.InboxAllocatedBundle {

    private final UUID bundleId;
    private final Inbox inbox;
    private final int fileCount;
    
    
    public InboxAllocatedBundleImpl(UUID bundleId, Inbox inbox, int fileCount) {
        this.bundleId = bundleId;
        this.inbox = inbox;
        this.fileCount = fileCount;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.AllocatedBundle#getBundleId()
     */
    @Override
    public UUID getBundleId() {
        return bundleId;
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
