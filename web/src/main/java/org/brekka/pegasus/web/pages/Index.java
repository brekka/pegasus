/**
 * 
 */
package org.brekka.pegasus.web.pages;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.web.upload.EncryptedFileItem;
import org.brekka.pegasus.core.model.AnonymousTransfer;
import org.brekka.pegasus.core.services.AnonymousService;
import org.brekka.pegasus.web.support.CompletedFileBuilders;
import org.got5.tapestry5.jquery.JQuerySymbolConstants;

/**
 * @author Andrew Taylor
 *
 */
@Import(library = {
    "${" + JQuerySymbolConstants.JQUERY_CORE_PATH + "}",
    "context:js/vendor/jquery.ui.widget.js",
    "context:js/jquery.iframe-transport.js",
    "context:js/jquery.fileupload.js",
    "context:js/upload.js"
})
public class Index {
    
    @Inject
    private RequestGlobals requestGlobals;
    
    @Inject
    private AlertManager alertManager;
    
    @Inject
    private AnonymousService anonymousService;
    
    @Property
    private UploadedFile file;
    
    @Property
    private String comment;
    
    void onSuccess() throws Exception {
        List<FileBuilder> fileBuilderList;
        if (file == null) {
            HttpServletRequest req = requestGlobals.getHTTPServletRequest();
            CompletedFileBuilders completedFileBuilders = CompletedFileBuilders.getCompletedFileBuilders(req, true);
            fileBuilderList = completedFileBuilders.retrieveAll();
        } else {
            Field field = file.getClass().getDeclaredField("item");
            field.setAccessible(true);
            Object object = field.get(file);
            if (object instanceof EncryptedFileItem) {
                EncryptedFileItem encryptedFileItem = (EncryptedFileItem) object;
                FileBuilder fileBuilder = encryptedFileItem.complete(null);
                fileBuilderList = Arrays.asList(fileBuilder);
            } else {
                throw new IllegalStateException();
            }
        }
        
        AnonymousTransfer transfer = anonymousService.createBundle(comment, "password", fileBuilderList);
        alertManager.info("Allocated bundle as '" + transfer.getSlug().getPath() + "'");
    }
}
