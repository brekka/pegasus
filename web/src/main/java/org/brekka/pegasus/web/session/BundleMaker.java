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
import org.brekka.pegasus.core.model.TransferKey;

/**
 * @author Andrew Taylor
 *
 */
public class BundleMaker {

    private final String makerKey;
    
    private final List<FileBuilder> completed = new ArrayList<>();
    
    private final Map<String, FileBuilder> inProgress = new HashMap<>();
    
    private TransferKey transferKey;
    
    private boolean done = false;
    
    /**
     * @param makerKey
     */
    public BundleMaker(String makerKey) {
        this.makerKey = makerKey;
    }
    
    public void retainInProgress(String fileName, FileBuilder fileBuilder) {
        inProgress.put(fileName, fileBuilder);
    }
    
    public FileBuilder retrieveInProgress(String fileName) {
        return inProgress.get(fileName);
    }
    
    public void makeComplete(FileBuilder fileBuilder) {
        inProgress.remove(fileBuilder.getFileName());
        completed.add(fileBuilder);
    }
    
    public boolean isDone() {
        return done;
    }
    
    public List<FileBuilder> previewCompleted() {
        return new ArrayList<>(completed);
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
    public void setTransferKey(TransferKey transferKey) {
        this.transferKey = transferKey;
    }
    
    /**
     * @return the transferKey
     */
    public TransferKey getTransferKey() {
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
