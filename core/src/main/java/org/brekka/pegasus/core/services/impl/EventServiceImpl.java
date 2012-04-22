/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.Date;
import java.util.UUID;

import org.brekka.pegasus.core.dao.BundleCreatedEventDAO;
import org.brekka.pegasus.core.dao.BundleUnlockEventDAO;
import org.brekka.pegasus.core.dao.FileDownloadEventDAO;
import org.brekka.pegasus.core.model.Bundle;
import org.brekka.pegasus.core.model.BundleCreatedEvent;
import org.brekka.pegasus.core.model.BundleUnlockEvent;
import org.brekka.pegasus.core.model.FileDownloadEvent;
import org.brekka.pegasus.core.model.RemoteUserEvent;
import org.brekka.pegasus.core.security.WebAuthenticationDetails;
import org.brekka.pegasus.core.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Andrew Taylor
 *
 */
@Service
@Transactional
public class EventServiceImpl implements EventService {

    @Autowired
    private FileDownloadEventDAO fileDownloadEventDAO;
    
    @Autowired
    private BundleUnlockEventDAO bundleUnlockEventDAO;
    
    @Autowired
    private BundleCreatedEventDAO bundleCreatedEventDAO;
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.EventService#bundleUnlocked(java.lang.String, java.lang.String, java.lang.String, org.brekka.pegasus.core.model.Bundle, java.util.Date)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void bundleUnlocked(Bundle bundle, Date agreementAccepted) {
        BundleUnlockEvent event = new BundleUnlockEvent();
        event.setBundle(bundle);
        populate(event);
        bundleUnlockEventDAO.create(event);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.EventService#beginFileDownloadEvent(java.lang.String, java.lang.String, java.lang.String, java.util.UUID)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public FileDownloadEvent beginFileDownloadEvent(UUID fileId) {
        FileDownloadEvent event = new FileDownloadEvent();
        event.setFileId(fileId);
        populate(event);
        fileDownloadEventDAO.create(event);
        return event;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.EventService#bundleCreated(java.lang.String, java.lang.String, java.lang.String, org.brekka.pegasus.core.model.Bundle)
     */
    @Override
    public void bundleCreated(Bundle bundle) {
        BundleCreatedEvent event = new BundleCreatedEvent();
        event.setBundle(bundle);
        populate(event);
        bundleCreatedEventDAO.create(event);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.EventService#completeEvent(org.brekka.pegasus.core.model.FileDownloadEvent)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void completeEvent(FileDownloadEvent event) {
        event.setCompleted(new Date());
        fileDownloadEventDAO.update(event);
    }
    
    protected void populate(RemoteUserEvent remoteUserEvent) {
        remoteUserEvent.setInitiated(new Date());
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        Object details = authentication.getDetails();
        if (details instanceof WebAuthenticationDetails) {
            WebAuthenticationDetails wad = (WebAuthenticationDetails) details;
            remoteUserEvent.setOnBehalfOfAddress(wad.getOnBehalfOfAddress());
            remoteUserEvent.setRemoteAddress(wad.getRemoteAddress());
            remoteUserEvent.setUserAgent(wad.getUserAgent());
        }
    }

}
