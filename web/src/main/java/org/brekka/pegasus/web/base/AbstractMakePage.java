/**
 * 
 */
package org.brekka.pegasus.web.base;

import java.lang.reflect.Field;
import java.text.Format;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.upload.services.UploadedFile;
import org.brekka.commons.lang.ByteLengthFormat;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.model.FileInfo;
import org.brekka.paveway.core.model.UploadPolicy;
import org.brekka.paveway.web.upload.EncryptedFileItem;
import org.brekka.pegasus.core.services.UploadPolicyService;
import org.brekka.pegasus.web.session.AllocationMaker;
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
    
    @Inject
    private UploadPolicyService uploadPolicyService;
    
    @Inject
    protected ComponentResources resources;
    
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
    
    @Property
    protected Format byteLengthFormat = new ByteLengthFormat(resources.getLocale(), ByteLengthFormat.Mode.SI);
    
    
    protected List<FileBuilder> processFiles(AllocationMaker bundleMaker) throws NoSuchFieldException,
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
    
    public UploadPolicy getUploadPolicy() {
        return uploadPolicyService.identifyUploadPolicy();
    }
    
    protected void init(String makeKey) {
        this.makeKey = makeKey;
    }
    
    public String getUploadLinkScript() {
        UploadPolicy policy = uploadPolicyService.identifyUploadPolicy();
        HttpServletRequest req = requestGlobals.getHTTPServletRequest();
        String uploadLink = req.getContextPath() + "/upload/" + makeKey;
        
        return String.format("<script>uploadLink = '%s', maxFileSize = %s, maxFiles = %s;</script>",
                uploadLink, policy.getMaxFileSize(), policy.getMaxFiles());
    }
}
