/**
 * 
 */
package org.brekka.pegasus.web.base;

import java.text.Format;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.RequestGlobals;
import org.brekka.commons.lang.ByteLengthFormat;
import org.brekka.paveway.core.model.CompletableUploadedFile;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.model.UploadedFiles;
import org.brekka.paveway.core.model.UploadPolicy;
import org.brekka.paveway.tapestry.components.Upload;
import org.brekka.paveway.web.session.UploadsContext;
import org.brekka.pegasus.web.pages.Index;
import org.brekka.pegasus.web.pages.NoSession;
import org.brekka.pegasus.web.support.MakeKeyUtils;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 * 
 */
public abstract class AbstractMakePage {
    
    @Inject
    protected RequestGlobals requestGlobals;

    @Inject
    protected AlertManager alertManager;
    
    @Inject
    private ComponentResources resources;
    
    @Component
    private Upload upload;

    @Property
    private String comment;

    @Property
    protected String makeKey;
    
    private UploadPolicy uploadPolicy;
    
    protected Format byteLengthFormat = new ByteLengthFormat(resources.getLocale(), ByteLengthFormat.Mode.SI);

    
    /**
     * Redirect assigning a new key
     * @return
     */
    protected Object activate() {
        this.makeKey = MakeKeyUtils.newKey();
        HttpServletRequest req = requestGlobals.getHTTPServletRequest();
        UploadsContext bundleMakerContext = UploadsContext.get(req, true);
        bundleMakerContext.get(makeKey);
        upload.init(makeKey);
        return this;
    }
    
    protected Object activate(String makeKey) {
        this.makeKey = makeKey;
        upload.init(makeKey);
        HttpServletRequest req = requestGlobals.getHTTPServletRequest();
        UploadsContext bundleMakerContext = UploadsContext.get(req, false);
        if (bundleMakerContext == null) {
            return NoSession.class;
        }
        if (!bundleMakerContext.contains(makeKey)) {
            alertManager.alert(Duration.SINGLE, Severity.WARN, "Sorry, but the uploaded uploadedFiles are no longer available. Please try again.");
            return activate();
        }
        UploadedFiles bundleMaker = bundleMakerContext.get(makeKey);
        setPolicy(bundleMaker.getPolicy());
        if (bundleMaker.isDone()) {
            // TODO
            return Index.class;
        }
        return Boolean.TRUE;
    }
    
    protected void setPolicy(UploadPolicy uploadPolicy) {
        this.uploadPolicy = uploadPolicy;
    }
    
    protected String passivate() {
        return makeKey;
    }
    
    protected void init(String makeKey) {
        this.makeKey = makeKey;
    }
    

    Object onSuccess() throws Exception {
        UploadedFiles files = upload.getValue();
        return onSuccess(comment, files);
    }
    
    protected abstract Object onSuccess(String comment, UploadedFiles files);
}
