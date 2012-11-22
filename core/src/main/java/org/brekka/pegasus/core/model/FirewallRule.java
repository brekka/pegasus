/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.brekka.commons.persistence.model.SnapshotEntity;
import org.brekka.pegasus.core.PegasusConstants;
import org.hibernate.annotations.Type;

/**
 * A rule within the firewall
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`FirewallRule`", schema=PegasusConstants.SCHEMA)
public class FirewallRule extends SnapshotEntity<UUID> {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 294799530375778897L;

    /**
     * Unique id
     */
    @Id
    @Type(type="pg-uuid")
    @Column(name="`ID`")
    private UUID id;
    
    /**
     * The firewall this rule belongs to.
     */
    @ManyToOne
    @JoinColumn(name="`FirewallID`", nullable=false)
    private Firewall firewall;
    
    /**
     * The group of networks which the rule will apply to.
     */
    @ManyToOne
    @JoinColumn(name="`NetworkGroupID`", nullable=false)
    private NetworkGroup networkGroup;
    
    /**
     * What action should be taken when this rule matches one or more of the networks in the group.
     */
    @Column(name="`Action`", length=5, nullable=false)
    @Enumerated(EnumType.STRING)
    private FirewallAction action;
    
    /**
     * Priority of this rule over others. Lower value = higher priority.
     */
    @Column(name="`Priority`")
    private int priority;

    /**
     * @return the id
     */
    public UUID getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(UUID id) {
        this.id = id;
    }

    public Firewall getFirewall() {
        return firewall;
    }

    public void setFirewall(Firewall firewall) {
        this.firewall = firewall;
    }

    public NetworkGroup getNetworkGroup() {
        return networkGroup;
    }

    public void setNetworkGroup(NetworkGroup networkGroup) {
        this.networkGroup = networkGroup;
    }

    public FirewallAction getAction() {
        return action;
    }

    public void setAction(FirewallAction action) {
        this.action = action;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
