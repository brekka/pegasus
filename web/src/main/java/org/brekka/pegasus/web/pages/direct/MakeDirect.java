/**
 * 
 */
package org.brekka.pegasus.web.pages.direct;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.services.AnonymousService;
import org.brekka.pegasus.web.base.AbstractMakePage;
import org.brekka.pegasus.web.pages.Index;
import org.brekka.pegasus.web.pages.NoSession;
import org.brekka.pegasus.web.session.AllocationMaker;
import org.brekka.pegasus.web.session.AllocationMakerContext;
import org.brekka.pegasus.web.support.MakeKeyUtils;
import org.brekka.xml.pegasus.v2.model.DetailsType;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class MakeDirect extends AbstractMakePage {

    @InjectPage
    private DirectDone directDonePage;
    
    @Inject
    private AnonymousService anonymousService;
    
    /**
     * Redirect assigning a new key
     * @return
     */
    Object onActivate() {
        String makeKey = MakeKeyUtils.newKey();
        HttpServletRequest req = requestGlobals.getHTTPServletRequest();
        AllocationMakerContext bundleMakerContext = AllocationMakerContext.get(req, true);
        bundleMakerContext.get(makeKey);
        init(makeKey);
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
        Object retVal;
        HttpServletRequest req = requestGlobals.getHTTPServletRequest();
        AllocationMakerContext bundleMakerContext = AllocationMakerContext.get(req, true);
        AllocationMaker bundleMaker = bundleMakerContext.get(makeKey);
        if (!bundleMaker.isDone()) {
            List<FileBuilder> fileBuilderList = processFiles(bundleMaker);
            DetailsType detailsType = DetailsType.Factory.newInstance();
            detailsType.setComment(comment);
            Allocation transferKey = anonymousService.createTransfer(detailsType, 1, 5, fileBuilderList, null);
            bundleMaker.setAllocation(transferKey);
            directDonePage.init(makeKey);
            retVal = directDonePage;
        } else {
            // TODO bad handling
            throw new IllegalStateException();
        }
        return retVal;
    }

}
