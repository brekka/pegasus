/**
 * 
 */
package org.brekka.pegasus.web.pages.dispatch;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.internal.OptionModelImpl;
import org.apache.tapestry5.internal.SelectModelImpl;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.paveway.core.model.CompletableUploadedFile;
import org.brekka.paveway.core.model.UploadedFiles;
import org.brekka.paveway.web.session.UploadsContext;
import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.model.AnonymousTransfer;
import org.brekka.pegasus.core.model.Deposit;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.DispatchService;
import org.brekka.pegasus.core.services.DivisionService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.OrganizationService;
import org.brekka.pegasus.core.services.VaultService;
import org.brekka.pegasus.core.support.AllocationDetailsBuilder;
import org.brekka.pegasus.web.base.AbstractMakePage;
import org.brekka.pegasus.web.pages.direct.DirectDone;
import org.brekka.xml.pegasus.v2.model.DetailsType;
import org.joda.time.DateTime;

/**
 * 
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class MakeDispatch extends AbstractMakePage {

    @InjectPage
    private DirectDone directDonePage;
    
    @InjectPage
    private DispatchDeposit dispatchDepositPage;
    
    @Inject
    private MemberService memberService;
    
    @Inject
    private VaultService vaultService;
    
    @Inject
    private OrganizationService organizationService;
    
    @Inject
    private DivisionService divisionService;
    
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
    
    @Property
    private int maxDownloads = 1;
    
    
    private Object[] context;
    
    Object onActivate(String orgToken, String divisionSlug, String makeKey) {
        Organization organization = organizationService.retrieveByToken(orgToken, false);
        Division<?> division = divisionService.retrieveDivision(organization, divisionSlug);
        return activate(makeKey, division, division, orgToken, divisionSlug);
    }
    
    Object onActivate(String vaultSlug, String makeKey) {
        Vault vault = vaultService.retrieveBySlug(vaultSlug);
        return activate(makeKey, null, vault, vaultSlug);
    }
    
    Object onActivate(String makeKey) {
        Vault activeVault = (Vault) memberService.getCurrent().getMember().getDefaultVault();
        return activate(makeKey, null, activeVault);
    }
    
    Object activate(String makeKey, Division<?> division, KeySafe<?> keySafe, Object... context) {
        this.division = division;
        this.keySafe = keySafe;
        HttpServletRequest req = requestGlobals.getHTTPServletRequest();
        UploadsContext uploadsContext = UploadsContext.get(req, true);
        UploadedFiles filesContext = uploadsContext.get(makeKey);
        this.context = ArrayUtils.add(context, makeKey);
        super.init(makeKey);
        setPolicy(filesContext.getPolicy());
        return Boolean.TRUE;
    }
    
    Object[] onPassivate() {
        return context;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.web.base.AbstractMakePage#onSuccess(java.util.List, java.lang.String)
     */
    @Override
    protected Object onSuccess(String comment, UploadedFiles files) {
        Object retVal;
        DetailsType detailsType = new AllocationDetailsBuilder<>(DetailsType.class)
            .setAgreementText(agreementText)
            .setReference(reference)
            .setComment(comment)
            .toDetailsType();
        
        // TODO Picked some numbers out of a hat
        DateTime dispatchExpires = new DateTime().plusDays(31);
        DateTime allocationExpires = new DateTime().plusDays(7);
        Allocation allocation = dispatchService.createDispatchAndAllocate(
                recipientEMail, division, keySafe, detailsType, dispatchExpires, allocationExpires, maxDownloads, files);
        files.addAttribute(Allocation.class.getName(), allocation);
        if (allocation instanceof Deposit) {
            dispatchDepositPage.init(makeKey, allocation);
            retVal = dispatchDepositPage;
        } else if (allocation instanceof AnonymousTransfer) {
            directDonePage.init(makeKey, allocation);
            retVal = directDonePage;
        } else {
            // TODO
            throw new IllegalStateException();
        }
        return retVal;
    }
    
    public SelectModel getDownloadOptionModel() {
        List<OptionModel> options = new ArrayList<>();
        options.add(new OptionModelImpl("1", 1));
        options.add(new OptionModelImpl("2", 2));
        options.add(new OptionModelImpl("3", 3));
        options.add(new OptionModelImpl("5", 5));
        return new SelectModelImpl(null, options);
    }
}
