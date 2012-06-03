/**
 * 
 */
package org.brekka.pegasus.web.support;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.mutable.MutableFloat;
import org.brekka.pegasus.core.model.AllocationFile;
import org.brekka.pegasus.core.model.Transfer;

/**
 * Transfers must be memory resident only
 * 
 * @author Andrew Taylor
 */
public class Transfers {

    private transient Map<String, Transfer> map;
    
    private transient Map<UUID, MutableFloat> progressMap;
    
    
    public void add(String token, Transfer bundle) {
        map().put(token, bundle);
    }
    
    public Transfer get(String token) {
        return map().get(token);
    }
    

    /**
     * @param bundleId
     * @return
     */
    public boolean contains(String token) {
        return map().containsKey(token);
    }
    
    public MutableFloat downloadStartProgress(AllocationFile bundleFile) {
        MutableFloat progress = new MutableFloat();
        progressMap().put(bundleFile.getId(), progress);
        return progress;
    }
    
    public MutableFloat getProgress(AllocationFile bundleFile) {
        return progressMap().get(bundleFile.getId());
    }
    
    /**
     * Ensures that a map is always available.
     * @return
     */
    protected Map<String, Transfer> map() {
        if (map == null) {
            map = new HashMap<String, Transfer>();
        }
        return map;
    }
    
    protected Map<UUID, MutableFloat> progressMap() {
        if (progressMap == null) {
            progressMap = new HashMap<UUID, MutableFloat>();
        }
        return progressMap;
    }

    /**
     * @param fromString
     * @return
     */
    public Transfer getTransferWithFile(UUID fileId) {
        Map<String, Transfer> map = map();
        Collection<Transfer> values = map.values();
        for (Transfer transfer : values) {
            Map<UUID, AllocationFile> files = transfer.getFiles();
            if (files.containsKey(fileId)) {
                return transfer;
            }
        }
        return null;
    }
}
