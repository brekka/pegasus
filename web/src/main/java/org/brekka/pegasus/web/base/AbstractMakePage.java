/**
 * 
 */
package org.brekka.pegasus.web.base;

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
import org.brekka.paveway.core.model.FileInfo;
import org.brekka.paveway.web.upload.EncryptedFileItem;
import org.brekka.pegasus.web.session.BundleMaker;
import org.got5.tapestry5.jquery.JQuerySymbolConstants;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 * 
 */
@Import(library = {
    "${" + JQuerySymbolConstants.JQUERY_CORE_PATH + "}",
    "context:js/vendor/jquery.ui.widget.js",
    "context:js/jquery.iframe-transport.js",
    "context:js/jquery.fileupload.js",
    "context:js/upload.js"
})
public abstract class AbstractMakePage {
    @Inject
    protected RequestGlobals requestGlobals;

    @Inject
    protected AlertManager alertManager;

    @Property
    protected UploadedFile file;

    @Property
    protected String comment;

    @Property
    protected String makeKey;

    @Property
    protected List<FileInfo> files;

    @Property
    protected FileInfo loopFile;

    protected List<FileBuilder> processFiles(BundleMaker bundleMaker) throws NoSuchFieldException,
            IllegalAccessException {
        List<FileBuilder> fileBuilderList;
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
        return fileBuilderList;
    }
    
    protected void init(String makeKey) {
        this.makeKey = makeKey;
    }
    
    public String getUploadLinkScript() {
        HttpServletRequest req = requestGlobals.getHTTPServletRequest();
        return "<script>uploadLink = '" + req.getContextPath() + "/upload/" + makeKey + "';</script>";
    }
}
