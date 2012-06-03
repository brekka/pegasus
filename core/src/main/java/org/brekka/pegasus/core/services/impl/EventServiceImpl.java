/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.Date;

import org.brekka.pegasus.core.dao.AgreementAcceptedEventDAO;
import org.brekka.pegasus.core.dao.BundleCreatedEventDAO;
import org.brekka.pegasus.core.dao.BundleUnlockEventDAO;
import org.brekka.pegasus.core.dao.FileDownloadEventDAO;
import org.brekka.pegasus.core.model.AgreementAcceptedEvent;
import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.Bundle;
import org.brekka.pegasus.core.model.BundleCreatedEvent;
import org.brekka.pegasus.core.model.BundleFile;
import org.brekka.pegasus.core.model.FileDownloadEvent;
import org.brekka.pegasus.core.model.RemoteUserEvent;
import org.brekka.pegasus.core.model.Transfer;
import org.brekka.pegasus.core.model.TransferUnlockEvent;
import org.brekka.pegasus.core.security.WebAuthenticationDetails;
import org.brekka.pegasus.core.services.EventService;
import org.brekka.pegasus.core.services.MemberService;
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
    
    @Autowired
    private AgreementAcceptedEventDAO agreementAcceptedEventDAO;
    
    @Autowired
    private MemberService memberService;
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.EventService#bundleUnlocked(java.lang.String, java.lang.String, java.lang.String, org.brekka.pegasus.core.model.Bundle, java.util.Date)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void transferUnlocked(Transfer transfer) {
        TransferUnlockEvent event = new TransferUnlockEvent();
        event.setTransfer(transfer);
        populate(event);
        bundleUnlockEventDAO.create(event);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.EventService#beginFileDownloadEvent(java.lang.String, java.lang.String, java.lang.String, java.util.UUID)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public FileDownloadEvent beginFileDownloadEvent(BundleFile bundleFile, Transfer transfer) {
        FileDownloadEvent event = new FileDownloadEvent();
        event.setBundleFile(bundleFile);
        event.setTransfer(transfer);
        populate(event);
        fileDownloadEventDAO.create(event);
        return event;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.EventService#fileDownloadCount(org.brekka.pegasus.core.model.BundleFile, org.brekka.pegasus.core.model.Transfer)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public int fileDownloadCount(BundleFile bundleFile, Transfer transfer) {
        return fileDownloadEventDAO.fileDownloadCount(bundleFile, transfer);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.EventService#isAccepted(org.brekka.pegasus.core.model.Bundle)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public boolean isAccepted(Transfer transfer) {
        AgreementAcceptedEvent event = agreementAcceptedEventDAO.retrieveByTransfer(transfer);
        return event != null;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.EventService#bundleCreated(java.lang.String, java.lang.String, java.lang.String, org.brekka.pegasus.core.model.Bundle)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
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
        populate(event);
        fileDownloadEventDAO.update(event);
    }
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void agreementAccepted(Transfer transfer) {
        AgreementAcceptedEvent event = new AgreementAcceptedEvent();
        event.setTransfer(transfer);
        populate(event);
        agreementAcceptedEventDAO.create(event);
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
        } else {
            throw new IllegalStateException("No web authentication details found.");
        }
        
        AuthenticatedMember current = memberService.getCurrent();
        if (current != null) {
            remoteUserEvent.setMember(current.getMember());
        }
    }

}
