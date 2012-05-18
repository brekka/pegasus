/**
 * 
 */
package org.brekka.pegasus.web.pages.dispatch;

import java.util.List;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.model.Dispatch;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.DispatchService;
import org.brekka.pegasus.core.services.OrganizationService;
import org.brekka.pegasus.core.services.VaultService;
import org.joda.time.DateTime;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public class DispatchListing {
    
    @Inject
    private VaultService vaultService;
    
    @Inject
    private OrganizationService organizationService;
    
    @Inject
    private DispatchService dispatchService;
    
    @Property
    private List<Dispatch> dispatches;
    
    @Property
    private Dispatch loopDispatch;
    
    Object onActivate(String orgToken, String divisionSlug, String interval) {
        Division division = organizationService.retrieveDivision(orgToken, divisionSlug);
        return activate(division, interval);
    }
    
    Object onActivate(String vaultSlug, String interval) {
        Vault vault = vaultService.retrieveBySlug(vaultSlug);
        return activate(vault, interval);
    }
    
    Object activate(KeySafe keySafe, String interval) {
        DateTime until = new DateTime();
        DateTime from = until.minusDays(1);
        dispatches = dispatchService.retrieveCurrentForInterval(keySafe, from, until);
        return Boolean.TRUE;
    }
}
