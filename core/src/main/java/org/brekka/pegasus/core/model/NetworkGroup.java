/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

/**
 * Groups one or more networks. Can be used to represent multiple networks
 * within an organization, country, or even continent.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`NetworkGroup`")
public class NetworkGroup extends LongevousEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -468824368378315219L;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NetworkGroupCategory getNetworkGroupCategory() {
        return networkGroupCategory;
    }

    public void setNetworkGroupCategory(NetworkGroupCategory networkGroupCategory) {
        this.networkGroupCategory = networkGroupCategory;
    }
}
