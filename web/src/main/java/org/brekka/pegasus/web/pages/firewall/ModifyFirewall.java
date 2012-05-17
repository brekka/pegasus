/**
 * 
 */
package org.brekka.pegasus.web.pages.firewall;

import java.util.List;
import java.util.UUID;

import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.model.Firewall;
import org.brekka.pegasus.core.model.FirewallAction;
import org.brekka.pegasus.core.model.FirewallRule;
import org.brekka.pegasus.core.model.NetworkGroup;
import org.brekka.pegasus.core.services.FirewallService;
import org.brekka.pegasus.web.pages.networkgroup.ModifyNetworkGroup;
import org.brekka.pegasus.web.support.NetworkGroupEncoder;
import org.brekka.pegasus.web.support.NetworkGroupSelectModelBuilder;

/**
 * Manipulate the firewall - add/remove rules.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class ModifyFirewall {
    @InjectPage
    private ModifyNetworkGroup modifyNetworkGroupPage;

    @Inject
    private FirewallService firewallService;

    @Inject
    private NetworkGroupSelectModelBuilder networkGroupSelectModelBuilder;

    @Inject
    private NetworkGroupEncoder networkGroupEncoder;

    @InjectComponent
    private Zone ruleZone;

    @InjectComponent
    private Zone customGroupZone;

    @Property
    private Firewall firewall;

    @Property
    private FirewallRule loopRule;

    @Property
    private String firewallName;

    @Property
    private FirewallAction firewallAction;
    
    @Property
    private String ruleName;

    @Property
    private NetworkGroup group;

    @Property
    private FirewallAction ruleAction;

    void onActivate(String firewallIdStr) {
        UUID firewallId = UUID.fromString(firewallIdStr);
        firewall = firewallService.retrieveFirewallById(firewallId);
        firewallName = firewall.getName();
        firewallAction = firewall.getDefaultAction();
    }

    String onPassivate() {
        return firewall.getId().toString();
    }
    
    Object onSuccessFromFirewall() {
        firewall = firewallService.updateFirewall(firewall.getId(), firewallName, firewallAction);
        return Boolean.TRUE;
    }

    Object onSuccessFromRule() {
        Object retVal;
        if (group == null) {
            // New custom rule
            FirewallRule rule = firewallService.createRule(ruleName, firewall, ruleAction, 1);
            modifyNetworkGroupPage.init(rule);
            retVal = modifyNetworkGroupPage;
        } else {
            firewallService.createRule(group, firewall, ruleAction, 1);
            group = null;
            ruleAction = null;
            retVal = ruleZone;
        }
        return retVal;
    }

    public Object onValueChanged(NetworkGroup networkGroup) {
        this.group = networkGroup;
        return customGroupZone.getBody();
    }
    
    Object onActionFromRemove(String ruleId) {
        UUID id = UUID.fromString(ruleId);
        firewallService.deleteRule(id);
        return ruleZone.getBody();
    }

    void init(Firewall firewall) {
        this.firewall = firewall;
    }

    public List<FirewallRule> getRules() {
        return firewallService.retrieveRules(firewall);
    }

    public boolean isEnableName() {
        return group == null;
    }

    public SelectModel getGroupModel() {
        return networkGroupSelectModelBuilder.getCategorized(firewall);
    }

    public ValueEncoder<NetworkGroup> getGroupEncoder() {
        return networkGroupEncoder;
    }
}
