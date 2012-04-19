/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.brekka.commons.persistence.model.IdentifiableEntity;
import org.hibernate.annotations.Type;

/**
 * @author Andrew Taylor
 *
 */
@Entity
@Table(name="\"Bundle\"")
public class Bundle extends IdentifiableEntity {

    /**
     * Id of the crypted data that contains the key used to encrypt this file's parts.
     */
    @Type(type="pg-uuid")
    @Column(name="CryptedDataID")
    private UUID cryptedDataId;
    
    /**
     * When does this bundle expire?
     */
    @Column(name="Expires")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expires;
    
    /**
     * When was this bundle actually deleted.
     */
    @Column(name="Deleted")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deleted;
    
    /**
     * The member who created this bundle (if available).
     */
    @ManyToOne
    @JoinColumn(name="MemberID")
    private Member member;
}
