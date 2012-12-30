/**
 * 
 */
package org.brekka.pegasus.web.pages.direct;

import java.util.List;

import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.paveway.core.model.CompletableUploadedFile;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.paveway.core.model.UploadedFiles;
import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.services.AnonymousService;
import org.brekka.pegasus.web.base.AbstractMakePage;
import org.brekka.xml.pegasus.v2.model.DetailsType;
import org.joda.time.DateTime;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class MakeDirect extends AbstractMakePage {

    @InjectPage
    private DirectDone directDonePage;
    
    @Inject
    private AnonymousService anonymousService;
    
    @Inject
    private AlertManager alertManager;
    
    Object onActivate() {
        return super.activate();
    }
    
    Object onActivate(String makeKey) {
        return super.activate(makeKey);
    }
    
    Object onPassivate() {
        return super.passivate();
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.web.base.AbstractMakePage#onSuccess(java.util.List, java.lang.String)
     */
    @Override
    protected Object onSuccess(String comment, UploadedFiles files) {
        DetailsType detailsType = DetailsType.Factory.newInstance();
        detailsType.setComment(comment);
        // TODO Expiry for transfers
        DateTime expires = new DateTime().plusDays(1);
        Allocation allocation = anonymousService.createTransfer(detailsType, expires, 1, 5, files, null);
        directDonePage.init(makeKey, allocation);
        return directDonePage;
    }

}
