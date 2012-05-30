/**
 * 
 */
package org.brekka.pegasus.web.pages.direct;

import java.util.Collection;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.model.AnonymousTransfer;
import org.brekka.pegasus.core.model.BundleFile;
import org.brekka.pegasus.core.services.AnonymousService;
import org.brekka.pegasus.core.services.BundleService;
import org.brekka.pegasus.web.support.Transfers;
import org.brekka.xml.pegasus.v1.model.BundleType;
import org.brekka.xml.pegasus.v1.model.FileType;

/**
 * @author Andrew Taylor
 *
 */
public class FetchDirect {
    
    @InjectPage
    private UnlockDirect unlockPage;
    
    @InjectPage
    private AgreementDirect agreementDirectPage;
    
    @Inject
    private AnonymousService anonymousService;
    
    @Inject
    private BundleService bundleService;

    @SessionAttribute("transfers")
    private Transfers transfers;
    
    @Property
    private String token;
    
    @Property
    private AnonymousTransfer transfer;
    
    @Property
    private BundleFile file;
    
    
    Object onActivate(String token) {
        this.token = token;
        
        if (transfers == null) {
            unlockPage.onActivate(token);
            return unlockPage;
        }
        
        Transfers transfers = this.transfers;
        transfer = (AnonymousTransfer) transfers.get(token);
        if (transfer == null) {
            unlockPage.onActivate(token);
            return unlockPage;
        }
        bundleService.refreshBundle(transfer.getBundle());
        BundleType bundleXml = transfer.getBundle().getXml();
        if (bundleXml.isSetAgreement() 
                && !anonymousService.isAccepted(transfer)) {
            agreementDirectPage.init(token);
            return agreementDirectPage;
        }
        return Boolean.TRUE;
    }
    
    public Collection<BundleFile> getFiles() {
        return transfer.getBundle().getFiles().values();
    }
    
    void init(String token) {
        this.token = token;
    }
    
    String onPassivate() {
        return token;
    }
    
    public String[] getFileContext() {
        FileType xml = file.getXml();
        return new String[]{ xml.getUUID(), xml.getName() };
    }
}
