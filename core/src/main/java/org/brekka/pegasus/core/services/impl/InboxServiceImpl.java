/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.io.IOException;
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
import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.core.model.InboxTransferKey;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.OpenVault;
import org.brekka.pegasus.core.model.Token;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.InboxService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.ProfileService;
import org.brekka.pegasus.core.services.TokenService;
import org.brekka.pegasus.core.services.VaultService;
import org.brekka.phalanx.api.beans.IdentityPrincipal;
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
    private VaultService vaultService;
    
    @Autowired
    private ProfileService profileService;
    
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#createInbox(java.lang.String, org.brekka.pegasus.core.model.Vault)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Inbox createInbox(String name, String introduction, String inboxToken, KeySafe keySafe) {
        Inbox inbox = new Inbox();
        Token token = tokenService.createForInbox(inboxToken);
        inbox.setToken(token);
        inbox.setIntroduction(introduction);
        inbox.setKeySafe(keySafe);
        inbox.setName(name);
        AuthenticatedMember authenticatedMember = memberService.getCurrent();
        Member member = authenticatedMember.getMember();
        inbox.setOwner(member);
        inboxDAO.create(inbox);
        
        InboxType newXmlInbox = authenticatedMember.getProfile().addNewInbox();
        newXmlInbox.setUUID(inbox.getId().toString());
        newXmlInbox.setName(name);
        profileService.currentUserProfileUpdated();
        
        return inbox;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#depositFiles(java.lang.String, java.lang.String, java.util.List)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public InboxTransferKey depositFiles(Inbox inbox, String reference, String comment, List<FileBuilder> fileBuilders) {
        // Bring the inbox under management
        inbox = inboxDAO.retrieveById(inbox.getId());
        Vault vault = (Vault) inbox.getKeySafe();
        UUID principalId = vault.getPrincipalId();
        
        Bundle bundleModel = new Bundle();
        bundleModel.setId(UUID.randomUUID());
        
        BundleDocument bundleDocument = prepareBundleDocument(comment, fileBuilders);
        BundleType bundleType = bundleDocument.getBundle();
        bundleType.setReference(reference);
        
        // Fetch the default crypto factory, generate a new secret key
        CryptoFactory defaultCryptoFactory = cryptoFactoryRegistry.getDefault();
        SecretKey secretKey = defaultCryptoFactory.getSymmetric().getKeyGenerator().generateKey();
        bundleModel.setProfile(defaultCryptoFactory.getProfileId());
        
        encryptBundleDocument(bundleDocument, bundleModel, secretKey);
        bundleDAO.create(bundleModel);
        
        CryptedData cryptedData = phalanxService.asymEncrypt(secretKey.getEncoded(), new IdentityPrincipal(principalId));
        
        Deposit deposit = new Deposit();
        deposit.setBundle(bundleModel);
        deposit.setInbox(inbox);
        deposit.setKeySafe(inbox.getKeySafe());
        deposit.setCryptedDataId(cryptedData.getId());
        
        depositDAO.create(deposit);
        
        return new InboxTransferKeyImp(bundleModel.getId(), inbox, fileBuilders.size());
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#retrieveForToken(java.lang.String)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Inbox retrieveForToken(String inboxToken) {
        Token token = tokenService.retrieveByPath(inboxToken);
        Inbox inbox = inboxDAO.retrieveByToken(token);
        
        AuthenticatedMember authenticatedMember = memberService.getCurrent();
        ProfileType profile = authenticatedMember.getProfile();
        for (int i = 0; i < profile.sizeOfInboxArray(); i++) {
            InboxType inboxXml = profile.getInboxArray(i);
            if (inboxXml.getUUID().equals(inbox.getId().toString())) {
                String name = inboxXml.getName();
                inbox.setName(name);
                break;
            }
        }
        return inbox;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#unlock(org.brekka.pegasus.core.model.Deposit)
     */
    @Override
    public BundleType unlock(Deposit deposit) {
        Bundle bundle = deposit.getBundle();
        UUID cryptedDataId = deposit.getCryptedDataId();
        
        AuthenticatedMember current = memberService.getCurrent();
        OpenVault activeVault = current.getActiveVault();
        
        byte[] secretKeyBytes = vaultService.releaseKey(cryptedDataId, activeVault);
        
        try {
            return decryptBundle(null, bundle, secretKeyBytes);
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
}
