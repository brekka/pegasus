/**
 * 
 */
package org.brekka.pegasus.web.support;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.brekka.pegasus.core.model.Bundle;
import org.brekka.pegasus.core.model.Transfer;
import org.brekka.xml.pegasus.v1.model.BundleType;
import org.brekka.xml.pegasus.v1.model.FileType;

/**
 * Transfers must be memory resident only
 * 
 * @author Andrew Taylor
 */
public class Transfers {

    private transient Map<String, Transfer> map;
    
    
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

    /**
     * @param fromString
     * @return
     */
    public FileType getFile(String fileId) {
        Map<String, Transfer> map = map();
        Collection<Transfer> values = map.values();
        for (Transfer transfer : values) {
            Bundle bundle = transfer.getBundle();
            BundleType bundleType = bundle.getXml();
            List<FileType> fileList = bundleType.getFileList();
            for (FileType fileType : fileList) {
                if (fileType.getUUID().equals(fileId)) {
                    return fileType;
                }
            }
        }
        return null;
    }
}
