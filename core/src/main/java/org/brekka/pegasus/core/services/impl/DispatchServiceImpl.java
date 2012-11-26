/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.pegasus.core.dao.DispatchDAO;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.model.Dispatch;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.EMailAddress;
import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.services.AnonymousService;
import org.brekka.pegasus.core.services.DispatchService;
import org.brekka.pegasus.core.services.EMailAddressService;
import org.brekka.pegasus.core.services.InboxService;
import org.brekka.pegasus.core.services.KeySafeService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.phalanx.api.model.CryptedData;
import org.brekka.phoenix.api.SecretKey;
import org.brekka.xml.pegasus.v1.model.AllocationDocument;
import org.brekka.xml.pegasus.v1.model.BundleType;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
@Service
@Transactional
public class DispatchServiceImpl extends AllocationServiceSupport implements DispatchService {

    @Autowired
    private MemberService memberService;
    
    @Autowired
    private EMailAddressService eMailAddressService;
    
    @Autowired
    private InboxService inboxService;
    
    @Autowired
    private AnonymousService anonymousService;
    
    @Autowired
    private KeySafeService keySafeService;
    
    @Autowired
    private DispatchDAO dispatchDAO;
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.DispatchService#createDispatch(java.lang.String, org.brekka.pegasus.core.model.Division, org.brekka.pegasus.core.model.KeySafe, java.lang.String, java.lang.String, java.lang.String, java.util.List)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Allocation createDispatch(String recipientEMail, Division division, KeySafe keySafe, String reference,
            String comment, String agreementText, int maxDownloads, List<FileBuilder> fileBuilderList) {
        Dispatch dispatch = new Dispatch();
        AuthenticatedMemberBase authenticatedMember = AuthenticatedMemberBase.getCurrent(memberService);
        Actor activeActor = authenticatedMember.getActiveActor();
        
        Inbox inbox = null;
        if (StringUtils.isNotBlank(recipientEMail)) {
            EMailAddress address = eMailAddressService.retrieveByAddress(recipientEMail);
            if (address != null) {
                // Known to the system.
                inbox = inboxService.retrieveForEMailAddress(address);
            }
        }
        
        BundleType bundleType = completeFiles(0, fileBuilderList);
        
        // Copy the allocation to
        AllocationDocument allocationDocument = prepareDocument(bundleType);
        setComment(comment, allocationDocument);
        setAgreementText(agreementText, allocationDocument);
        setReference(reference, allocationDocument);
        encryptDocument(dispatch, allocationDocument);
        
        
        dispatch.setDivision(division);
        dispatch.setKeySafe(keySafe);
        dispatch.setActor(activeActor);
        
        SecretKey secretKey = dispatch.getSecretKey();
        dispatch.setSecretKey(null);
        
        CryptedData cryptedData = keySafeService.protect(secretKey.getEncoded(), keySafe);
        dispatch.setCryptedDataId(cryptedData.getId());
        
        dispatchDAO.create(dispatch);
        createAllocationFiles(dispatch);
        
        BundleType cloneBundle = copyBundle(maxDownloads, bundleType);
        
        Allocation allocation;
        if (inbox != null) {
            allocation = inboxService.createDeposit(inbox, reference, comment, agreementText, cloneBundle, dispatch);
        } else {
            allocation = anonymousService.createTransfer(comment, agreementText, cloneBundle, dispatch);
        }
        return allocation;
    }


    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.DispatchService#retrieveCurrentForInterval(org.joda.time.DateTime, org.joda.time.DateTime)
     */
    @Override
    public List<Dispatch> retrieveCurrentForInterval(KeySafe keySafe, DateTime from, DateTime until) {
        AuthenticatedMemberBase authenticatedMember = AuthenticatedMemberBase.getCurrent(memberService);
        Actor activeActor = authenticatedMember.getActiveActor();
        return dispatchDAO.retrieveForInterval(keySafe, activeActor, from.toDate(), until.toDate());
    }
}
