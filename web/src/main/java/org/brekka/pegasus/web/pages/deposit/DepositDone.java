/**
 * 
 */
package org.brekka.pegasus.web.pages.deposit;

import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.model.Deposit;
import org.brekka.pegasus.web.pages.Index;
import org.brekka.pegasus.web.support.AllocationContext;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class DepositDone {
    
    @Inject
    private AlertManager alertManager;
    
    @Persist
    private AllocationContext allocationContext;
    
    @Property
    private String makeKey;
    
    @Property
    private Deposit deposit;
    
    
    
    Object onActivate(String makeKey) {
        this.makeKey = makeKey;
        
        if (allocationContext.has(makeKey)) {
            deposit = allocationContext.get(makeKey, Deposit.class);
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
}
