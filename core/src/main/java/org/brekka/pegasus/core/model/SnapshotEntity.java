/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.brekka.commons.persistence.model.IdentifiableEntity;

/**
 * An entity that doesn't really change after it is created
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@MappedSuperclass
public class SnapshotEntity extends IdentifiableEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 310200006809589397L;

    /**
     * The moment this entity was created
     */
    @Column(name="`Created`", nullable=false, updatable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    

    public Date getCreated() {
        return created;
    }
    
    /**
     * @param created the created to set
     */
    public void setCreated(Date created) {
        this.created = created;
    }
}
