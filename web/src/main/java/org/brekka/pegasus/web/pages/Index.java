/**
 * 
 */
package org.brekka.pegasus.web.pages;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.web.upload.EncryptedFileItem;
import org.brekka.pegasus.core.model.AnonymousTransfer;
import org.brekka.pegasus.core.services.AnonymousService;

/**
 * @author Andrew Taylor
 *
 */
public class Index {
    
    @Inject
    private AlertManager alertManager;
    
    @Inject
    private AnonymousService anonymousService;
    
    @Property
    private UploadedFile file;
    
    @Property
    private String comment;
    
    void onSuccess() throws Exception {
        Field field = file.getClass().getDeclaredField("item");
        field.setAccessible(true);
        Object object = field.get(file);
        if (object instanceof EncryptedFileItem) {
            EncryptedFileItem encryptedFileItem = (EncryptedFileItem) object;
            FileBuilder fileBuilder = encryptedFileItem.complete(null);
            
            AnonymousTransfer transfer = anonymousService.createBundle(comment, "password", Arrays.asList(fileBuilder));
            
            alertManager.info("Allocated bundle as '" + transfer.getSlug().getPath() + "'");
        }
    }
}
