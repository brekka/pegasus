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
 * Identifies entities that will be around for a long time, ie are lively to be updated at some
 * point during their (long) lifetime.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
@MappedSuperclass
public abstract class LongevousEntity extends IdentifiableEntity {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 4541242370565729473L;

    @Column(name="`Created`", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name="`Modified`", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;


    public Date getCreated() {
        return created;
    }

    public Date getModified() {
        return modified;
    }
    
    /**
     * @param created the created to set
     */
    void setCreated(Date created) {
        this.created = created;
    }
    
    /**
     * @param modified the modified to set
     */
    void setModified(Date modified) {
        this.modified = modified;
    }
}
