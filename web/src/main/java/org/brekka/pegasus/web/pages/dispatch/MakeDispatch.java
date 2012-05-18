/**
 * 
 */
package org.brekka.pegasus.web.pages.dispatch;

import java.lang.reflect.Array;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.pegasus.core.model.AllocatedBundle;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.AnonymousService;
import org.brekka.pegasus.core.services.DispatchService;
import org.brekka.pegasus.core.services.InboxService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.OrganizationService;
import org.brekka.pegasus.core.services.VaultService;
import org.brekka.pegasus.web.base.AbstractMakePage;
import org.brekka.pegasus.web.session.BundleMaker;
import org.brekka.pegasus.web.session.BundleMakerContext;

/**
 * 
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class MakeDispatch extends AbstractMakePage {

    @InjectPage
    private DispatchDirect dispatchDirectPage;
    
    @InjectPage
    private DispatchDeposit dispatchDepositPage;
    
    @Inject
    private MemberService memberService;
    
    @Inject
    private VaultService vaultService;
    
    @Inject
    private OrganizationService organizationService;
    
    @Inject
    private DispatchService dispatchService;
    
    @Property
    private Division division;
    
    @Property
    private KeySafe keySafe;
    
    
    @Property
    private String recipientEMail;
    
    @Property
    private String reference;
    
    @Property
    private String agreementText;
    
    
    private Object[] context;
    
    Object onActivate(String orgToken, String divisionSlug, String makeKey) {
        Division division = organizationService.retrieveDivision(orgToken, divisionSlug);
        return activate(makeKey, division, division, orgToken, divisionSlug);
    }
    
    Object onActivate(String vaultSlug, String makeKey) {
        Vault vault = vaultService.retrieveBySlug(vaultSlug);
        return activate(makeKey, null, vault, vaultSlug);
    }
    
    Object onActivate(String makeKey) {
        Vault activeVault = memberService.getCurrent().getActiveVault();
        return activate(makeKey, null, activeVault);
    }
    
    Object activate(String makeKey, Division division, KeySafe keySafe, Object... context) {
        this.division = division;
        this.keySafe = keySafe;
        HttpServletRequest req = requestGlobals.getHTTPServletRequest();
        BundleMakerContext bundleMakerContext = BundleMakerContext.get(req, true);
        bundleMakerContext.get(makeKey);
        this.context = ArrayUtils.add(context, makeKey);
        super.init(makeKey);
        return Boolean.TRUE;
    }
    
    Object[] onPassivate() {
        return context;
    }
    
    Object onSuccess() throws Exception {
        Object retVal;
        HttpServletRequest req = requestGlobals.getHTTPServletRequest();
        BundleMakerContext bundleMakerContext = BundleMakerContext.get(req, true);
        BundleMaker bundleMaker = bundleMakerContext.get(makeKey);
        if (!bundleMaker.isDone()) {
            List<FileBuilder> fileBuilderList = processFiles(bundleMaker);
            AllocatedBundle transferKey = dispatchService.createDispatch(
                    recipientEMail, division, keySafe, reference, comment, agreementText, fileBuilderList);
            bundleMaker.setTransferKey(transferKey);
            if (transferKey instanceof InboxService.InboxAllocatedBundle) {
                dispatchDepositPage.init(makeKey);
                retVal = dispatchDepositPage;
            } else if (transferKey instanceof AnonymousService.AnonymousAllocatedBundle) {
                dispatchDirectPage.init(makeKey);
                retVal = dispatchDirectPage;
            } else {
                // TODO
                throw new IllegalStateException();
            }
        } else {
            // TODO bad handling
            throw new IllegalStateException();
        }
        return retVal;
    }
}
