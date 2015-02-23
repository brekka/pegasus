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
import javax.persistence.Table;

import org.brekka.commons.persistence.model.SnapshotEntity;
import org.brekka.pegasus.core.PegasusConstants;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

/**
 * A set of rules that can be used to control what remote users can access resources based on their IP address.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`Firewall`", schema=PegasusConstants.SCHEMA)
public class Firewall extends SnapshotEntity<UUID> {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 4426014107250666833L;

    /**
     * Unique id
     */
    @Id
    @Type(type="pg-uuid")
    @Access(AccessType.PROPERTY)
    @Column(name="`ID`")
    private UUID id;

    /**
     * The entity that owns this firewall ruleset.
     * For example a member, division or even the system id.
     */
    @Column(name="`OwningEntityId`", nullable=false)
    @Type(type="pg-uuid")
    @Index(name="IDX_Firewall_Owner")
    private UUID owningEntityId;

    /**
     * Optional name for this firewall.
     */
    @Column(name="`Name`", length=128)
    private String name;

    /**
     * The default action to be performed if no rules are matched.
     */
    @Column(name="`DefaultAction`", length=5, nullable=false)
    @Enumerated(EnumType.STRING)
    private FirewallAction defaultAction;

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

    public UUID getOwningEntityId() {
        return this.owningEntityId;
    }

    public void setOwningEntityId(final UUID owningEntityId) {
        this.owningEntityId = owningEntityId;
    }

    public FirewallAction getDefaultAction() {
        return this.defaultAction;
    }

    public void setDefaultAction(final FirewallAction defaultAction) {
        this.defaultAction = defaultAction;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
