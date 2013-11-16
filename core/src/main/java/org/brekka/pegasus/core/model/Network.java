/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.brekka.commons.persistence.model.SnapshotEntity;
import org.brekka.pegasus.core.PegasusConstants;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

/**
 * A network block such as "10.0.0.0/8".
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`Network`", schema=PegasusConstants.SCHEMA)
public class Network extends SnapshotEntity<UUID> {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -9155812902107593391L;

    /**
     * Unique id
     */
    @Id
    @Type(type="pg-uuid")
    @Column(name="`ID`")
    private UUID id;

    /**
     * The group that this network block belongs to.
     */
    @ManyToOne()
    @JoinColumn(name="`NetworkGroupID`", nullable=false, updatable=false)
    private NetworkGroup networkGroup;

    /**
     * The CIDR network block, e.g. "10.0.0.0/8"
     */
    @Column(name="`Block`", columnDefinition="cidr", nullable=false)
    @Type(type="org.brekka.pegasus.core.support.CidrUserType")
    @Index(name="IDX_Network_Block")
    private String block;

    public NetworkGroup getNetworkGroup() {
        return networkGroup;
    }

    public void setNetworkGroup(final NetworkGroup networkGroup) {
        this.networkGroup = networkGroup;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(final String block) {
        this.block = block;
    }

    /**
     * @return the id
     */
    @Override
    public UUID getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    @Override
    public void setId(final UUID id) {
        this.id = id;
    }
}
