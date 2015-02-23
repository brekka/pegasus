/**
 *
 */
package org.brekka.pegasus.core.model;

import java.util.UUID;

import javax.persistence.Access;
import javax.persistence.AccessType;
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
    @Access(AccessType.PROPERTY)
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
    @Override
    public UUID getId() {
        return this.id;
    }

    /**
     * @param id the id to set
     */
    @Override
    public void setId(final UUID id) {
        this.id = id;
    }

    public Firewall getFirewall() {
        return this.firewall;
    }

    public void setFirewall(final Firewall firewall) {
        this.firewall = firewall;
    }

    public NetworkGroup getNetworkGroup() {
        return this.networkGroup;
    }

    public void setNetworkGroup(final NetworkGroup networkGroup) {
        this.networkGroup = networkGroup;
    }

    public FirewallAction getAction() {
        return this.action;
    }

    public void setAction(final FirewallAction action) {
        this.action = action;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(final int priority) {
        this.priority = priority;
    }
}
