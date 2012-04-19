/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.brekka.commons.persistence.model.IdentifiableEntity;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

/**
 * Tracks when an actual file is downloaded, including the IP information etc.
 * 
 * @author Andrew Taylor
 *
 */
@Entity
@Table(name="\"FileDownloadEvent\"")
public class FileDownloadEvent extends RemoteUserEvent {

    @Type(type="pg-uuid")
    @Column(name="FileID")
    @Index(name="IDX_FileDownloadId")
    private UUID fileId;
    
    /**
     * The moment the last byte was sent
     */
    @Column(name="Completed")
    @Temporal(TemporalType.TIMESTAMP)
    private Date completed; 
}
