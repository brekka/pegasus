/**
 * 
 */
package org.brekka.pegasus.web.pages.direct;

import java.text.Format;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.commons.lang.ByteLengthFormat;
import org.brekka.pegasus.core.model.AllocationFile;
import org.brekka.pegasus.core.model.AnonymousTransfer;
import org.brekka.pegasus.core.services.AllocationService;
import org.brekka.pegasus.core.services.AnonymousService;
import org.brekka.pegasus.web.support.Transfers;
import org.brekka.xml.pegasus.v1.model.AllocationType;
import org.brekka.xml.pegasus.v1.model.FileType;

/**
 * @author Andrew Taylor
 *
 */
public class FetchDirect {
    
    private static final int MAX_FILE_NAME_LENGTH = 64;
    
    @InjectPage
    private UnlockDirect unlockPage;
    
    @InjectPage
    private AgreementDirect agreementDirectPage;
    
    @Inject
    private AnonymousService anonymousService;
    
    @Inject
    private AllocationService allocationService;
    
    @Inject
    private ComponentResources resources;

    @SessionAttribute("transfers")
    private Transfers transfers;
    
    @Property
    private String token;
    
    @Property
    private AnonymousTransfer transfer;
    
    @Property
    private AllocationFile file;
    
    @SuppressWarnings("unused")
    @Property
    private Format byteLengthFormat = new ByteLengthFormat(resources.getLocale(), ByteLengthFormat.Mode.SI);
    
    
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
        allocationService.refreshAllocation(transfer);
        AllocationType xml = transfer.getXml();
        if (xml.isSetAgreement() 
                && !anonymousService.isAccepted(transfer)) {
            agreementDirectPage.init(token);
            return agreementDirectPage;
        }
        return Boolean.TRUE;
    }
    
    public Collection<AllocationFile> getFiles() {
        return transfer.getFiles().values();
    }
    
    public boolean isLastAttempt() {
        int downloadsForTransfer = file.getDownloadCount();
        return downloadsForTransfer + 1 == file.getXml().getMaxDownloads();
    }
    
    public String getFileName() {
        String name = file.getXml().getName();
        String prefix = StringUtils.substringBeforeLast(name, ".");
        String extension = StringUtils.substringAfterLast(name, ".");
        if (prefix.length() > MAX_FILE_NAME_LENGTH) {
            prefix = StringUtils.abbreviate(prefix, MAX_FILE_NAME_LENGTH);
        }
        return prefix + (extension != null ? "." + extension : "");
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
    
    public String getFileClass() {
        return file.getDeleted() == null ? "available" : "deleted";
    }
}
