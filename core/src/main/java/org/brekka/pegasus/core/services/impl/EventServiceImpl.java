/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.Date;
import java.util.UUID;

import org.brekka.pegasus.core.dao.BundleUnlockEventDAO;
import org.brekka.pegasus.core.dao.FileDownloadEventDAO;
import org.brekka.pegasus.core.model.Bundle;
import org.brekka.pegasus.core.model.BundleUnlockEvent;
import org.brekka.pegasus.core.model.FileDownloadEvent;
import org.brekka.pegasus.core.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.EventService#bundleUnlocked(java.lang.String, java.lang.String, java.lang.String, org.brekka.pegasus.core.model.Bundle, java.util.Date)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void bundleUnlocked(String remoteAddress, String onBehalfOfAddress, String userAgent, Bundle bundle,
            Date agreementAccepted) {
        BundleUnlockEvent event = new BundleUnlockEvent();
        event.setRemoteAddress(remoteAddress);
        event.setInitiated(new Date());
        event.setOnBehalfOfAddress(onBehalfOfAddress);
        event.setUserAgent(userAgent);
        event.setBundle(bundle);
        bundleUnlockEventDAO.create(event);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.EventService#beginFileDownloadEvent(java.lang.String, java.lang.String, java.lang.String, java.util.UUID)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public FileDownloadEvent beginFileDownloadEvent(String remoteAddress, String onBehalfOfAddress, String userAgent,
            UUID fileId) {
        FileDownloadEvent event = new FileDownloadEvent();
        event.setRemoteAddress(remoteAddress);
        event.setInitiated(new Date());
        event.setOnBehalfOfAddress(onBehalfOfAddress);
        event.setUserAgent(userAgent);
        event.setFileId(fileId);
        fileDownloadEventDAO.create(event);
        return event;
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

}
