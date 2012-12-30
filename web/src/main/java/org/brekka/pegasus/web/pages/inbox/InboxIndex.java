/**
 * 
 */
package org.brekka.pegasus.web.pages.inbox;

import java.text.Format;
import java.util.List;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.commons.lang.ByteLengthFormat;
import org.brekka.commons.tapestry.support.ElapsedPeriodFormat;
import org.brekka.pegasus.core.model.Deposit;
import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.core.services.InboxService;
import org.brekka.pegasus.web.support.Configuration;
import org.brekka.xml.pegasus.v2.model.AllocationType;
import org.brekka.xml.pegasus.v2.model.DetailsType;
import org.brekka.xml.pegasus.v2.model.FileType;

/**
 * List the deposits in the index
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class InboxIndex {
    @Inject
    private Configuration configuration;
    
    @Inject
    private InboxService inboxService;
    
    @Inject
    private ComponentResources resources;
    
    @Property
    private Inbox inbox;
    
    private Deposit loopDeposit;
    
    @Property
    private FileType loopFile;
    
    @SuppressWarnings("unused")
    @Property
    private Format byteLengthFormat = new ByteLengthFormat(resources.getLocale(), ByteLengthFormat.Mode.SI);
    
    @SuppressWarnings("unused")
    @Property
    private Format elapsedPeriodFormat = ElapsedPeriodFormat.getDateTimeInstance(resources.getLocale(), resources.getMessages());
    
    Object onActivate(String token) {
        inbox = inboxService.retrieveForToken(token);
        return Boolean.TRUE;
    }
    
    String onPassivate() {
        return inbox.getToken().getPath();
    }
    
    public List<Deposit> getDeposits() {
        return inboxService.retrieveDeposits(inbox, false);
    }
    
    public String getInboxLink() {
        return configuration.getFetchBase() + "/" + inbox.getToken().getPath(); 
    }
    
    public String[] getFileContext() {
        return new String[]{ loopFile.getUUID(), loopFile.getName() };
    }
    
    /**
     * @param loopDeposit the loopDeposit to set
     */
    public void setLoopDeposit(Deposit loopDeposit) {
        this.loopDeposit = inboxService.retrieveDeposit(loopDeposit.getId());
    }
    
    /**
     * @return the loopDeposit
     */
    public Deposit getLoopDeposit() {
        return loopDeposit;
    }
    
    public AllocationType getXml() {
        return loopDeposit.allocationType();
    }
}
