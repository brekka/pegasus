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
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.model.FileInfo;
import org.brekka.paveway.web.upload.EncryptedFileItem;
import org.brekka.pegasus.core.model.TransferKey;
import org.brekka.pegasus.core.services.AnonymousService;
import org.brekka.pegasus.web.session.BundleMaker;
import org.brekka.pegasus.web.session.BundleMakerContext;
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
public class Make {
    
    @InjectPage
    private Allocated allocatedPage;
    
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
    
    @Property
    private String makeKey;
    
    @Property
    private List<FileInfo> files;
    
    @Property
    private FileInfo loopFile;
    
    
    Object onActivate(String makeKey) {
        this.makeKey = makeKey;
        HttpServletRequest req = requestGlobals.getHTTPServletRequest();
        BundleMakerContext bundleMakerContext = BundleMakerContext.get(req, true);
        BundleMaker bundleMaker = bundleMakerContext.get(makeKey);
        if (bundleMaker.isDone()) {
            return Index.class;
        }
        files = bundleMaker.previewCompleted();
        
        return Boolean.TRUE;
    }

    String onPassivate() {
        return makeKey;
    }

    Object onSuccess() throws Exception {
        List<FileBuilder> fileBuilderList;
        HttpServletRequest req = requestGlobals.getHTTPServletRequest();
        BundleMakerContext bundleMakerContext = BundleMakerContext.get(req, true);
        BundleMaker bundleMaker = bundleMakerContext.get(makeKey);
        if (!bundleMaker.isDone()) {
            if (file == null) {
                fileBuilderList = bundleMaker.complete();
            } else {
                bundleMaker.discard();
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
            TransferKey transferKey = anonymousService.createBundle(comment, fileBuilderList);
            bundleMaker.setTransferKey(transferKey);
        }
        allocatedPage.onActivate(makeKey);
        return allocatedPage;
    }
    
    public String getUploadLinkScript() {
        HttpServletRequest req = requestGlobals.getHTTPServletRequest();
        return "<script>uploadLink = '" + req.getContextPath() + "/upload/" + makeKey + "';</script>";
    }
}
