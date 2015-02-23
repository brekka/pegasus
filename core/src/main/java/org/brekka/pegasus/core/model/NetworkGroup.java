/**
 *
 */
package org.brekka.pegasus.core.model;

import java.util.List;
import java.util.UUID;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.brekka.commons.persistence.model.LongevousEntity;
import org.brekka.pegasus.core.PegasusConstants;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

/**
 * Groups one or more networks. Can be used to represent multiple networks
 * within an organization, country, or even continent.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`NetworkGroup`", schema=PegasusConstants.SCHEMA)
public class NetworkGroup extends LongevousEntity<UUID> {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -468824368378315219L;

    /**
     * Unique id
     */
    @Id
    @Type(type="pg-uuid")
    @Access(AccessType.PROPERTY)
    @Column(name="`ID`")
    private UUID id;

    /**
     * The name of this network group, e.g. England
     */
    @Column(name="`Name`", length=128)
    private String name;

    /**
     * Apply a category to this group such as if this is a country.
     */
    @Column(name="`Type`", length=16)
    @Enumerated(EnumType.STRING)
    @Index(name="IDX_NetworkGroup_Category")
    private NetworkGroupCategory networkGroupCategory;

    @OneToMany(mappedBy = "networkGroup", cascade = {CascadeType.REMOVE}, fetch=FetchType.LAZY)
    private List<Network> networks;

    @OneToMany(mappedBy = "networkGroup", fetch=FetchType.LAZY)
    private List<FirewallRule> rules;


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

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public NetworkGroupCategory getNetworkGroupCategory() {
        return this.networkGroupCategory;
    }

    public void setNetworkGroupCategory(final NetworkGroupCategory networkGroupCategory) {
        this.networkGroupCategory = networkGroupCategory;
    }

    public List<Network> getNetworks() {
        return this.networks;
    }

    public void setNetworks(final List<Network> networks) {
        this.networks = networks;
    }

    public List<FirewallRule> getRules() {
        return this.rules;
    }

    public void setRules(final List<FirewallRule> rules) {
        this.rules = rules;
    }
}
