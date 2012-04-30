/**
 * 
 */
package org.brekka.pegasus.web.support;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.brekka.xml.pegasus.v1.model.BundleType;
import org.brekka.xml.pegasus.v1.model.FileType;

/**
 * Bundles must be memory resident only
 * 
 * @author Andrew Taylor
 */
public class Bundles {

    private transient Map<String, BundleType> map;
    
    
    public void add(String token, BundleType bundle) {
        map().put(token, bundle);
    }
    
    public BundleType get(String token) {
        return map().get(token);
    }
    
    /**
     * Ensures that a map is always available.
     * @return
     */
    protected Map<String, BundleType> map() {
        if (map == null) {
            map = new HashMap<String, BundleType>();
        }
        return map;
    }

    /**
     * @param fromString
     * @return
     */
    public FileType getFile(String fileId) {
        Map<String, BundleType> map = map();
        Collection<BundleType> values = map.values();
        for (BundleType bundleType : values) {
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
