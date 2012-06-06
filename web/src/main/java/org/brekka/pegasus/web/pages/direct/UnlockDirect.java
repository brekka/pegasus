/**
 * 
 */
package org.brekka.pegasus.web.pages.direct;

import javax.inject.Inject;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.brekka.pegasus.core.model.AnonymousTransfer;
import org.brekka.pegasus.core.services.AnonymousService;

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
    
    @Property
    private String token;

    @Property
    private String code;
    
    void onActivate(String token) {
        this.token = token;
    }
    
    String onPassivate() {
        return token;
    }
    
    Object onSuccess() {
        AnonymousTransfer anonymousTransfer = anonymousService.unlock(token, code);
        fetchPage.init(token);
        if (anonymousTransfer.getXml().isSetAgreement()) {
            agreementDirectPage.init(token);
            return agreementDirectPage;
        }
        return fetchPage;
    }
}
