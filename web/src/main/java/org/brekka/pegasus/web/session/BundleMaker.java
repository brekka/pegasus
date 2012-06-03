/**
 * 
 */
package org.brekka.pegasus.web.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.model.FileInfo;
import org.brekka.paveway.core.model.FilesContext;
import org.brekka.paveway.core.model.UploadPolicy;
import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.core.model.AllocatedBundle;

/**
 * @author Andrew Taylor
 *
 */
public class BundleMaker implements FilesContext {

    private final String makerKey;
    
    private final UploadPolicy policy;
    
    private final List<FileBuilder> completed = new ArrayList<>();
    
    private final Map<String, FileBuilder> inProgress = new HashMap<>();
    
    private AllocatedBundle transferKey;
    
    private boolean done = false;
    
    private final Inbox inbox;
    
    /**
     * @param makerKey
     */
    public BundleMaker(String makerKey, UploadPolicy policy) {
        this(makerKey, policy, null);
    }
    
    /**
     * @param makerKey
     */
    public BundleMaker(String makerKey, UploadPolicy policy, Inbox inbox) {
        this.makerKey = makerKey;
        this.policy = policy;
        this.inbox = inbox;
    }
    
    public synchronized boolean isFileSlotAvailable() {
        if (done) {
            return false;
        }
        return policy.getMaxFiles() > (completed.size() + inProgress.size());
    }
    
    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.FilesContext#retain(java.lang.String, org.brekka.paveway.core.model.FileBuilder)
     */
    @Override
    public void retain(String fileName, FileBuilder fileBuilder) {
        inProgress.put(fileName, fileBuilder);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.FilesContext#retrieve(java.lang.String)
     */
    @Override
    public FileBuilder retrieve(String fileName) {
        return inProgress.get(fileName);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.FilesContext#complete(org.brekka.paveway.core.model.FileBuilder)
     */
    @Override
    public void complete(FileBuilder fileBuilder) {
        inProgress.remove(fileBuilder.getFileName());
        completed.add(fileBuilder);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.paveway.core.model.FilesContext#getPolicy()
     */
    @Override
    public UploadPolicy getPolicy() {
        return policy;
    }
    
    public boolean isDone() {
        return done;
    }
    
    /**
     * @return the inbox
     */
    public Inbox getInbox() {
        return inbox;
    }
    
    public List<FileInfo> previewCompleted() {
        return new ArrayList<FileInfo>(completed);
    }
    
    public synchronized List<FileBuilder> complete() {
        // Deallocate the files that were never completed
        Collection<FileBuilder> values = inProgress.values();
        discardAll(values);
        inProgress.clear();
        
        List<FileBuilder> fileBuilders = new ArrayList<>(completed);
        completed.clear();
        done = true;
        return fileBuilders;
    }
    
    /**
     * @param transferKey the transferKey to set
     */
    public void setTransferKey(AllocatedBundle transferKey) {
        this.transferKey = transferKey;
    }
    
    /**
     * @return the transferKey
     */
    public AllocatedBundle getTransferKey() {
        return transferKey;
    }
    
    /**
     * @return the makerKey
     */
    public String getKey() {
        return makerKey;
    }
    
    
    public synchronized void discard() {
        Collection<FileBuilder> values = inProgress.values();
        discardAll(values);
        inProgress.clear();
        discardAll(completed);
        completed.clear();
    }
    
    private void discardAll(Collection<FileBuilder> fileBuilders) {
        for (FileBuilder fileBuilder : fileBuilders) {
            fileBuilder.discard();
        }
    }

}
