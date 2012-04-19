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
 * @author Andrew Taylor
 *
 */
@MappedSuperclass
public abstract class RemoteUserEvent extends IdentifiableEntity {
    
    /**
     * Serial UID
     */
    private static final long serialVersionUID = 497882239907363162L;

    /**
     * The moment the event occurred
     */
    @Column(name="Initiated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date initiated; 
    
    /**
     * IP address of the system this web server talked to.
     */
    @Column(name="RemoteAddress")
    private String remoteAddress;
    
    /**
     * If the remote party was behind a proxy and it reported the internal IP,
     * this will record that IP.
     */
    @Column(name="OnBehalfOfAddress")
    private String onBehalfOfAddress;

    /**
     * The user agent reported by the remote server
     */
    @Column(name="UserAgent")
    private String userAgent;
}
