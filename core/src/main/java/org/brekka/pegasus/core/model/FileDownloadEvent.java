/**
 * 
 */
package org.brekka.pegasus.core.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.brekka.pegasus.core.PegasusConstants;
import org.hibernate.annotations.Index;

/**
 * Tracks when an actual file is downloaded, including the IP information etc.
 * 
 * @author Andrew Taylor
 *
 */
@Entity
@Table(name="`FileDownloadEvent`", schema=PegasusConstants.SCHEMA)
public class FileDownloadEvent extends RemoteUserEvent {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -2521026919756337883L;

    /**
     * The file that was downloaded
     */
    @ManyToOne()
    @JoinColumn(name="`TransferFileID`", nullable=false)
    @Index(name="IDX_FileDownloadEvent_01")
    private AllocationFile transferFile;
    
    
    /**
     * The moment the last byte was sent
     */
    @Column(name="`Completed`")
    @Temporal(TemporalType.TIMESTAMP)
    private Date completed;



    public AllocationFile getTransferFile() {
        return transferFile;
    }

    public void setTransferFile(AllocationFile transferFile) {
        this.transferFile = transferFile;
    }

    public Date getCompleted() {
        return completed;
    }

    public void setCompleted(Date completed) {
        this.completed = completed;
    } 
}
