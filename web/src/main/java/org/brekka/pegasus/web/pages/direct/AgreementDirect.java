/**
 * 
 */
package org.brekka.pegasus.web.pages.direct;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.model.AnonymousTransfer;
import org.brekka.pegasus.core.services.AnonymousService;
import org.brekka.pegasus.web.support.Transfers;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class AgreementDirect {
    
    @InjectPage
    private UnlockDirect unlockPage;
    
    @InjectPage
    private FetchDirect fetchDirectPage;
    
    @Inject
    private AnonymousService anonymousService;
    
    @SessionAttribute("transfers")
    private Transfers transfers;
    
    @Property
    private boolean agree;
    
    @Property
    private String token;
    
    @Property
    private AnonymousTransfer transfer;
    
    Object onActivate(String token) {
        this.token = token;
        
        if (transfers == null) {
            unlockPage.onActivate(token);
            return unlockPage;
        }
        
        transfer = (AnonymousTransfer) transfers.get(token);
        if (transfer == null) {
            unlockPage.onActivate(token);
            return unlockPage;
        }
        if (anonymousService.isAccepted(transfer)) {
            fetchDirectPage.init(token);
            return fetchDirectPage;
        }
        return Boolean.TRUE;
    }
    
    String onPassivate() {
        return token;
    }
    
    Object onSuccess() {
        if (agree) {
            anonymousService.agreementAccepted(token);
            fetchDirectPage.init(token);
            return fetchDirectPage;
        }
        return Boolean.TRUE;
    }
    
    void init(String token) {
        this.token = token;
    }
    
    public String getAgreementText() {
        return transfer.getBundle().getXml().getAgreement();
    }
}
