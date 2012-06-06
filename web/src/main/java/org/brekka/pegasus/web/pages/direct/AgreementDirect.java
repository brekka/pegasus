/**
 * 
 */
package org.brekka.pegasus.web.pages.direct;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.model.AnonymousTransfer;
import org.brekka.pegasus.core.services.AnonymousService;

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
    
    @Property
    private boolean agree;
    
    @Property
    private String token;
    
    @Property
    private AnonymousTransfer transfer;
    
    Object onActivate(String token) {
        this.token = token;
        
        transfer = anonymousService.retrieveTransfer(token);
        
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
        return transfer.getXml().getAgreement();
    }
}
