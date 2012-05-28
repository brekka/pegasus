/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.apache.xmlbeans.XmlException;
import org.brekka.paveway.core.model.FileBuilder;
import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.dao.DepositDAO;
import org.brekka.pegasus.core.dao.InboxDAO;
import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.Bundle;
import org.brekka.pegasus.core.model.Deposit;
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
import org.brekka.phoenix.CryptoFactory;
import org.brekka.xml.pegasus.v1.model.BundleDocument;
import org.brekka.xml.pegasus.v1.model.BundleType;
import org.brekka.xml.pegasus.v1.model.InboxType;
import org.brekka.xml.pegasus.v1.model.ProfileType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Service
@Transactional
public class InboxServiceImpl extends PegasusServiceSupport implements InboxService {

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
    public InboxAllocatedBundle depositFiles(Inbox inbox, String reference, String comment, String agreementText, List<FileBuilder> fileBuilders) {
        // Bring the inbox under management
        inbox = inboxDAO.retrieveById(inbox.getId());
        KeySafe keySafe = inbox.getKeySafe();
        
        Bundle bundleModel = new Bundle();
        bundleModel.setId(UUID.randomUUID());
        
        BundleDocument bundleDocument = prepareBundleDocument(comment, agreementText, fileBuilders);
        BundleType bundleType = bundleDocument.getBundle();
        bundleType.setReference(reference);
        
        // Fetch the default crypto factory, generate a new secret key
        CryptoFactory defaultCryptoFactory = cryptoFactoryRegistry.getDefault();
        SecretKey secretKey = defaultCryptoFactory.getSymmetric().getKeyGenerator().generateKey();
        bundleModel.setProfile(defaultCryptoFactory.getProfileId());
        
        encryptBundleDocument(bundleDocument, bundleModel, secretKey);
        bundleDAO.create(bundleModel);
        
        CryptedData cryptedData = keySafeService.protect(secretKey.getEncoded(), keySafe);
        
        Deposit deposit = new Deposit();
        deposit.setBundle(bundleModel);
        deposit.setInbox(inbox);
        deposit.setKeySafe(keySafe);
        deposit.setCryptedDataId(cryptedData.getId());
        
        depositDAO.create(deposit);
        
        return new InboxAllocatedBundleImpl(bundleModel, secretKey, inbox, fileBuilders.size());
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
    public Deposit unlock(Deposit deposit) {
        Bundle bundle = deposit.getBundle();
        UUID cryptedDataId = deposit.getCryptedDataId();
        
        KeySafe keySafe = deposit.getKeySafe();
        
        byte[] secretKeyBytes = keySafeService.release(cryptedDataId, keySafe);
        
        try {
            BundleType bundleType = decryptTransfer(deposit, secretKeyBytes);
            bundle.setXml(bundleType);
            return deposit;
        } catch (XmlException | IOException e) {
            throw new PegasusException(PegasusErrorCode.PG200, e, 
                    "Failed to retrieve bundle XML for deposit '%s'" , deposit.getId());
        }
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
