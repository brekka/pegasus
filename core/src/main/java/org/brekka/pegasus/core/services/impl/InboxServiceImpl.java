/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.pegasus.core.dao.DepositDAO;
import org.brekka.pegasus.core.dao.InboxDAO;
import org.brekka.pegasus.core.model.AccessorContext;
import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.Deposit;
import org.brekka.pegasus.core.model.Dispatch;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.EMailAddress;
import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Token;
import org.brekka.pegasus.core.model.TokenType;
import org.brekka.pegasus.core.services.InboxService;
import org.brekka.pegasus.core.services.KeySafeService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.ProfileService;
import org.brekka.pegasus.core.services.TokenService;
import org.brekka.phalanx.api.model.CryptedData;
import org.brekka.phoenix.api.SecretKey;
import org.brekka.xml.pegasus.v2.model.AllocationDocument;
import org.brekka.xml.pegasus.v2.model.AllocationType;
import org.brekka.xml.pegasus.v2.model.BundleType;
import org.brekka.xml.pegasus.v2.model.InboxType;
import org.brekka.xml.pegasus.v2.model.ProfileType;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Service
@Transactional
public class InboxServiceImpl extends AllocationServiceSupport implements InboxService {

    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private InboxDAO inboxDAO;
    
    @Autowired
    private DepositDAO depositDAO;
    
    @Autowired
    private MemberService memberService;
    
    @Autowired
    private KeySafeService keySafeService;
    
    @Autowired
    private ProfileService profileService;
    
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#createInbox(java.lang.String, org.brekka.pegasus.core.model.Vault)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Inbox createInbox(String name, String introduction, String inboxToken, KeySafe keySafe) {
        Inbox inbox = new Inbox();
        inbox.setId(UUID.randomUUID());
        Token token = tokenService.createToken(inboxToken, TokenType.INBOX);
        inbox.setToken(token);
        inbox.setIntroduction(introduction);
        inbox.setKeySafe(keySafe);
        inbox.setName(name);
        AuthenticatedMember authenticatedMember = memberService.getCurrent();
        Member member = authenticatedMember.getMember();
        if (keySafe instanceof Division) {
            inbox.setDivision((Division) keySafe);
        } else {
            inbox.setOwner(member);
            InboxType newXmlInbox = authenticatedMember.getProfile().addNewInbox();
            newXmlInbox.setUUID(inbox.getId().toString());
            newXmlInbox.setName(name);
            profileService.currentUserProfileUpdated();
        }
        inboxDAO.create(inbox);
        
        
        return inbox;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#depositFiles(java.lang.String, java.lang.String, java.util.List)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Deposit createDeposit(Inbox inbox, String reference, String comment, String agreementText, 
            List<FileBuilder> fileBuilders) {
        BundleType bundleType = completeFiles(0, fileBuilders);
        return createDeposit(inbox, reference, comment, agreementText, bundleType, null);
    }
    
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Deposit createDeposit(Inbox inbox, String reference, String comment, String agreementText, 
            BundleType bundleType, Dispatch dispatch) {
        Deposit deposit = new Deposit();
        deposit.setDerivedFrom(dispatch);
        
        // Bring the inbox under management
        inbox = inboxDAO.retrieveById(inbox.getId());
        KeySafe keySafe = inbox.getKeySafe();
        
        // TODO Expiry, currently fixed at one week, should be configured.
        DateTime now = new DateTime();
        DateTime expires = now.plusDays(30);
        deposit.setExpires(expires.toDate());
        
        AllocationDocument document = prepareDocument(bundleType);
        AllocationType allocationType = document.getAllocation();
        if (StringUtils.isNotBlank(comment)) {
            allocationType.setComment(comment);
        }
        if (StringUtils.isNotBlank(agreementText)) {
            allocationType.setAgreement(agreementText);
        }
        if (StringUtils.isNotBlank(reference)) {
            allocationType.setReference(reference);
        }
        
        // Encrypt the document
        encryptDocument(deposit, document);
        SecretKey secretKey = deposit.getSecretKey();
        deposit.setSecretKey(null);
        
        CryptedData cryptedData = keySafeService.protect(secretKey.getEncoded(), keySafe);
        deposit.setCryptedDataId(cryptedData.getId());
        
        deposit.setInbox(inbox);
        deposit.setKeySafe(keySafe);
        deposit.setSecretKey(secretKey);
        
        createAllocationFiles(deposit);
        depositDAO.create(deposit);
        
        return deposit;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#retrieveForToken(java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Inbox retrieveForToken(String inboxToken) {
        Token token = tokenService.retrieveByPath(inboxToken);
        Inbox inbox = inboxDAO.retrieveByToken(token);
        populateNames(Arrays.asList(inbox));
        return inbox;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#retrieveForVault(org.brekka.pegasus.core.model.Vault)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public List<Inbox> retrieveForKeySafe(KeySafe keySafe) {
        List<Inbox> inboxList = inboxDAO.retrieveForKeySafe(keySafe);
        populateNames(inboxList);
        return inboxList;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#retrieveForEMailAddress(org.brekka.pegasus.core.model.EMailAddress)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Inbox retrieveForEMailAddress(EMailAddress eMailAddress) {
        Inbox inbox = inboxDAO.retrieveForEMailAddress(eMailAddress);
        return inbox;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#retrieveForDivision(org.brekka.pegasus.core.model.DivisionAssociate)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public List<Inbox> retrieveForDivision(Division division) {
        return inboxDAO.retrieveForDivision(division);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#unlock(org.brekka.pegasus.core.model.Deposit)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Deposit retrieveDeposit(UUID depositId) {
        AccessorContext current = AccessorContext.getCurrent();
        Deposit deposit = current.retrieve(depositId, Deposit.class);
        if (deposit == null) {
            // Need to extract the metadata
            deposit = depositDAO.retrieveById(depositId);
            UUID cryptedDataId = deposit.getCryptedDataId();
            if (cryptedDataId != null) {
                KeySafe keySafe = deposit.getKeySafe();
                byte[] secretKeyBytes = keySafeService.release(cryptedDataId, keySafe);
                decryptDocument(deposit, secretKeyBytes);
                bindToContext(deposit);
            }
        } else {
            // Already unlocked, just refresh
            refreshAllocation(deposit);
        }
        return deposit;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#retrieveForMember()
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public List<Inbox> retrieveForMember() {
        AuthenticatedMember authenticatedMember = memberService.getCurrent();
        List<Inbox> inboxList = inboxDAO.retrieveForMember(authenticatedMember.getMember());
        populateNames(inboxList);
        return inboxList;
    }

    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#retrieveDeposits(org.brekka.pegasus.core.model.Inbox)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public List<Deposit> retrieveDeposits(Inbox inbox) {
        return depositDAO.retrieveByInbox(inbox);
    }
    
    private void populateNames(List<Inbox> inboxList) {
        AuthenticatedMember authenticatedMember = memberService.getCurrent();
        if (authenticatedMember == null) {
            return;
        }
        ProfileType profile = authenticatedMember.getProfile();
        if (profile != null) {
            for (Inbox inbox : inboxList) {
                for (int i = 0; i < profile.sizeOfInboxArray(); i++) {
                    InboxType inboxXml = profile.getInboxArray(i);
                    if (inboxXml.getUUID().equals(inbox.getId().toString())) {
                        String name = inboxXml.getName();
                        inbox.setName(name);
                        break;
                    }
                }
            }
        }
    }
}
