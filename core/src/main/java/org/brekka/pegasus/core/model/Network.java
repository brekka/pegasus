/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

/**
 * A network block such as "10.0.0.0/8".
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`Network`")
public class Network extends SnapshotEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -9155812902107593391L;

    /**
     * The group that this network block belongs to.
     */
    @ManyToOne
    @JoinColumn(name="`NetworkGroupID`", nullable=false, updatable=false)
    private NetworkGroup networkGroup;
    
    /**
     * The CIDR network block, e.g. "10.0.0.0/8"
     */
    @Column(name="`Block`", columnDefinition="cidr", nullable=false)
    @Index(name="IDX_Network_Block")
    private String block;
    

    public NetworkGroup getNetworkGroup() {
        return networkGroup;
    }

    public void setNetworkGroup(NetworkGroup networkGroup) {
        this.networkGroup = networkGroup;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }
}
