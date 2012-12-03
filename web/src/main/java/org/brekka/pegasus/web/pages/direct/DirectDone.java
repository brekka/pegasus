/**
 * 
 */
package org.brekka.pegasus.web.pages.direct;

import java.util.List;

import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.RequestGlobals;
import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.model.AnonymousTransfer;
import org.brekka.pegasus.web.pages.Index;
import org.brekka.pegasus.web.support.AllocationContext;
import org.brekka.pegasus.web.support.Configuration;
import org.brekka.xml.pegasus.v2.model.FileType;

/**
 * @author Andrew Taylor
 *
 */
public class DirectDone {
    
    @Inject
    private Configuration configuration;
    
    @Inject
    private AlertManager alertManager;
    
    @Persist
    private AllocationContext allocationContext;
    
    @Inject
    private RequestGlobals requestGlobals;
    
    @Property
    private String makeKey;
    
    @Property
    private AnonymousTransfer transfer;
    
    
    Object onActivate(String makeKey) {
        this.makeKey = makeKey;
        if (allocationContext.has(makeKey)) {
            transfer = allocationContext.get(makeKey,AnonymousTransfer.class);
            return Boolean.TRUE;
        }
        alertManager.alert(Duration.SINGLE, Severity.WARN, "Details of the requested upload are no longer available.");
        return Index.class;
    }
    
    public void init(String makeKey, Allocation allocation) {
        this.makeKey = makeKey;
        if (allocationContext == null) {
            allocationContext = new AllocationContext();
        }
        allocationContext.register(makeKey, allocation);
    }
    
    String onPassivate() {
        return makeKey;
    }
    
    public String getUnlockLink() {
        return configuration.getFetchBase() + "/" + transfer.getToken().getPath(); 
    }
    
    /**
     * /cxt/qr/code/token/filename
     * @return
     */
    public String getQrCodeImageLink() {
        String cxtPath = requestGlobals.getRequest().getContextPath();
        String path = String.format("%s/qr/%s", cxtPath, getPath());
        return path;
    }
    
    public String getDirectLink() {
        return configuration.getFetchBase() + "/" + getPath();
    }
    
    public boolean isAgreement() {
        return transfer.getXml().getDetails().isSetAgreement();
    }
    
    private String getPath() {
        String code = transfer.getCode().replaceAll("[^0-9]+", "");
        String path;
        List<FileType> fileList = transfer.getXml().getBundle().getFileList();
        if (fileList.size() == 1) {
            String fileName = fileList.get(0).getName();
            path = code + "/" + transfer.getToken().getPath() + "/" + fileName;
        } else {
            path = code + "/" + transfer.getToken().getPath() + ".zip";
        }
        return path;
    }
}
