/**
 * 
 */
package org.brekka.pegasus.web.pages.direct;

import java.text.Format;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.RequestGlobals;
import org.brekka.commons.lang.ByteLengthFormat;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.model.UploadPolicy;
import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.services.AnonymousService;
import org.brekka.pegasus.web.components.MultiUpload;
import org.brekka.pegasus.web.pages.Index;
import org.brekka.pegasus.web.pages.NoSession;
import org.brekka.pegasus.web.session.AllocationMaker;
import org.brekka.pegasus.web.session.AllocationMakerContext;
import org.brekka.pegasus.web.support.MakeKeyUtils;
import org.brekka.xml.pegasus.v2.model.DetailsType;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class MakeDirect {

    @InjectPage
    private DirectDone directDonePage;
    
    @Inject
    private AnonymousService anonymousService;
    
    @Inject
    private RequestGlobals requestGlobals;
    
    @Inject
    private AlertManager alertManager;
    
    @Inject
    private ComponentResources resources;
    
    @Component
    protected MultiUpload files;
    
    private String makeKey;
    

    @Property
    protected String comment;
    
    private UploadPolicy uploadPolicy;
    
    
    protected Format byteLengthFormat = new ByteLengthFormat(resources.getLocale(), ByteLengthFormat.Mode.SI);
    
    /**
     * Redirect assigning a new key
     * @return
     */
    Object onActivate() {
        this.makeKey = MakeKeyUtils.newKey();
        HttpServletRequest req = requestGlobals.getHTTPServletRequest();
        AllocationMakerContext bundleMakerContext = AllocationMakerContext.get(req, true);
        bundleMakerContext.get(makeKey);
        files.init(makeKey);
        return this;
    }
    
    Object onActivate(String makeKey) {
        this.makeKey = makeKey;
        HttpServletRequest req = requestGlobals.getHTTPServletRequest();
        AllocationMakerContext bundleMakerContext = AllocationMakerContext.get(req, false);
        if (bundleMakerContext == null) {
            return NoSession.class;
        }
        if (!bundleMakerContext.contains(makeKey)) {
            alertManager.alert(Duration.SINGLE, Severity.WARN, "Sorry, but the uploaded files are no longer available. Please try again.");
            return onActivate();
        }
        AllocationMaker bundleMaker = bundleMakerContext.get(makeKey);
        uploadPolicy = bundleMaker.getPolicy();
        if (bundleMaker.isDone()) {
            return Index.class;
        }
        return Boolean.TRUE;
    }
    
    String onPassivate() {
        return makeKey;
    }

    Object onSuccess() throws Exception {
        List<FileBuilder> fileBuilderList = files.getValue();
        DetailsType detailsType = DetailsType.Factory.newInstance();
        detailsType.setComment(comment);
        Allocation allocation = anonymousService.createTransfer(detailsType, 1, 5, fileBuilderList, null);
        // TODO Don't pass the allocation to the unlock via the allocationMaker, think of another way!
        files.getAllocationMaker().setAllocation(allocation);
        directDonePage.init(makeKey);
        return directDonePage;
    }
    
    public String getMultiDescription() {
        return String.format("Click 'Browse...' above to add one or more files, or drag the files onto this window. " +
        		"The files will begin to upload immediately but will not become available for download until " +
        		"\"Make available\" is clicked in section 3. You can upload up to <em>%d</em> files, where each " +
        		"file can be at most <em>%s</em>", uploadPolicy.getMaxFiles(), 
        		byteLengthFormat.format(uploadPolicy.getMaxFileSize()));
    }

}
