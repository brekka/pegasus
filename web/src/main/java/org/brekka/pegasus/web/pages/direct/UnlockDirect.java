/**
 * 
 */
package org.brekka.pegasus.web.pages.direct;

import javax.inject.Inject;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.brekka.pegasus.core.model.AnonymousTransfer;
import org.brekka.pegasus.core.services.AnonymousService;
import org.brekka.pegasus.web.support.Transfers;

/**
 * @author Andrew Taylor
 *
 */
public class UnlockDirect {
    public static final String PATH = "direct/unlock";
    
    @InjectPage
    private FetchDirect fetchPage;
    
    @InjectPage
    private AgreementDirect agreementDirectPage;
    
    @Inject
    private AnonymousService anonymousService;
    
    @SessionAttribute("transfers")
    private Transfers transfers;
    
    @Property
    private String token;

    @Property
    private String code;
    
    void onActivate(String token) {
        this.token = token;
        if (transfers == null) {
            transfers = new Transfers();
        }
    }
    
    String onPassivate() {
        return token;
    }
    
    Object onSuccess() {
        AnonymousTransfer anonymousTransfer = anonymousService.unlock(token, code);
        transfers.add(token, anonymousTransfer);
        fetchPage.init(token);
        if (anonymousTransfer.getXml().isSetAgreement()) {
            agreementDirectPage.init(token);
            return agreementDirectPage;
        }
        return fetchPage;
    }
}
