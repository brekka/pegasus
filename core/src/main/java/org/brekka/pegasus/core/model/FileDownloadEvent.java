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

import org.hibernate.annotations.Index;

/**
 * Tracks when an actual file is downloaded, including the IP information etc.
 * 
 * @author Andrew Taylor
 *
 */
@Entity
@Table(name="`FileDownloadEvent`")
public class FileDownloadEvent extends RemoteUserEvent {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -2521026919756337883L;

    /**
     * The file that was downloaded
     */
    @ManyToOne
    @JoinColumn(name="`BundleFileID`", nullable=false)
    @Index(name="IDX_FileDownloadEvent_01", columnNames={ "`BundleFileID`", "`TransferID`" })
    private BundleFile bundleFile;
    
    /**
     * Which transfer was the download for
     */
    @ManyToOne
    @JoinColumn(name="`TransferID`", nullable=false)
    private Transfer transfer;
    
    /**
     * The moment the last byte was sent
     */
    @Column(name="`Completed`")
    @Temporal(TemporalType.TIMESTAMP)
    private Date completed;


    public Transfer getTransfer() {
        return transfer;
    }

    public void setTransfer(Transfer transfer) {
        this.transfer = transfer;
    }

    public BundleFile getBundleFile() {
        return bundleFile;
    }

    public void setBundleFile(BundleFile bundleFile) {
        this.bundleFile = bundleFile;
    }

    public Date getCompleted() {
        return completed;
    }

    public void setCompleted(Date completed) {
        this.completed = completed;
    } 
}
