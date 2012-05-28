/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.List;

import javax.crypto.SecretKey;

import org.apache.commons.lang3.StringUtils;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.pegasus.core.dao.DispatchDAO;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.AllocatedBundle;
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
public class DispatchServiceImpl extends PegasusServiceSupport implements DispatchService {

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
    public AllocatedBundle createDispatch(String recipientEMail, Division division, KeySafe keySafe, String reference,
            String comment, String agreementText, List<FileBuilder> fileBuilderList) {
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
        AllocatedBundle bundle;
        if (inbox != null) {
            bundle = inboxService.depositFiles(inbox, reference, comment, agreementText, fileBuilderList);
        } else {
            bundle = anonymousService.createBundle(comment, agreementText, fileBuilderList);
        }
        
        AbstractAllocatedBundle allocatedBundle = (AbstractAllocatedBundle) bundle;
        
        dispatch.setDivision(division);
        dispatch.setKeySafe(keySafe);
        dispatch.setActor(activeActor);
        dispatch.setBundle(allocatedBundle.getBundle());
        
        SecretKey secretKey = allocatedBundle.removeSecretKey();
        CryptedData cryptedData = keySafeService.protect(secretKey.getEncoded(), keySafe);
        dispatch.setCryptedDataId(cryptedData.getId());
        
        dispatchDAO.create(dispatch);
        return bundle;
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
