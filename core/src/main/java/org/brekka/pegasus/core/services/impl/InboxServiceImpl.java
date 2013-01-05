/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.model.ListingCriteria;
import org.brekka.commons.persistence.support.EntityUtils;
import org.brekka.paveway.core.model.UploadedFiles;
import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.dao.DepositDAO;
import org.brekka.pegasus.core.dao.InboxDAO;
import org.brekka.pegasus.core.event.VaultDeleteEvent;
import org.brekka.pegasus.core.model.AccessorContext;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.AllocationDisposition;
import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.Deposit;
import org.brekka.pegasus.core.model.Dispatch;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.EMailAddress;
import org.brekka.pegasus.core.model.Inbox;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Token;
import org.brekka.pegasus.core.model.PegasusTokenType;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.services.InboxService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.ProfileService;
import org.brekka.pegasus.core.services.TokenService;
import org.brekka.xml.pegasus.v2.model.AllocationDocument;
import org.brekka.xml.pegasus.v2.model.AllocationType;
import org.brekka.xml.pegasus.v2.model.BundleType;
import org.brekka.xml.pegasus.v2.model.DetailsType;
import org.brekka.xml.pegasus.v2.model.InboxType;
import org.brekka.xml.pegasus.v2.model.ProfileType;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Service
@Transactional
public class InboxServiceImpl extends AllocationServiceSupport implements InboxService, ApplicationListener<ApplicationEvent>  {

    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private InboxDAO inboxDAO;
    
    @Autowired
    private DepositDAO depositDAO;
    
    @Autowired
    private MemberService memberService;
    
    @Autowired
    private ProfileService profileService;
    
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#createInbox(java.lang.String, org.brekka.pegasus.core.model.Vault)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Inbox createInbox(String name, String introduction, String inboxToken, KeySafe<? extends Actor> keySafe) {
        Inbox inbox = new Inbox();
        inbox.setId(UUID.randomUUID());
        if (inboxToken != null) {
            Token token = tokenService.createToken(inboxToken, PegasusTokenType.INBOX);
            inbox.setToken(token);
        }
        inbox.setIntroduction(introduction);
        inbox.setKeySafe(keySafe);
        inbox.setName(name);
        inbox.setOwner(keySafe.getOwner());
        if (keySafe instanceof Division) {
            inbox.setDivision((Division<?>) keySafe);
        } else if (keySafe instanceof Vault) {
            AuthenticatedMember<Member> authenticatedMember = memberService.getCurrent(Member.class);
            if (authenticatedMember != null
                    && EntityUtils.identityEquals(authenticatedMember.getMember(), keySafe.getOwner())) {
                InboxType newXmlInbox = authenticatedMember.getProfile().addNewInbox();
                newXmlInbox.setUUID(inbox.getId().toString());
                newXmlInbox.setName(name);
                profileService.currentUserProfileUpdated();
            }
        }
        inboxDAO.create(inbox);
        return inbox;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#depositFiles(java.lang.String, java.lang.String, java.util.List)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Deposit createDeposit(Inbox inbox, AllocationDisposition disposition, DetailsType details, DateTime expires, 
            UploadedFiles files) {
        BundleType bundleType = completeFiles(0, files);
        return createDeposit(inbox, disposition, details, expires, null, bundleType);
    }
    
    
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Deposit createDeposit(Inbox inbox, AllocationDisposition disposition, DetailsType details, DateTime expires, Dispatch dispatch) {
        BundleType dispatchBundle = copyDispatchBundle(dispatch, null);
        return createDeposit(inbox, disposition, details, expires, dispatch, dispatchBundle);
    }

    protected Deposit createDeposit(Inbox inbox, AllocationDisposition disposition, DetailsType details, DateTime expires, Dispatch dispatch, BundleType newBundleType) {   
        Deposit deposit = new Deposit();
        deposit.setDerivedFrom(dispatch);
        
        // Bring the inbox under management
        inbox = inboxDAO.retrieveById(inbox.getId());
        KeySafe<?> keySafe = inbox.getKeySafe();
        deposit.setExpires(expires.toDate());
        
        AllocationType allocationType = prepareAllocationType(newBundleType, details);
        
        // Encrypt the document
        encryptDocument(deposit, allocationType, keySafe);
        
        deposit.setInbox(inbox);
        deposit.setKeySafe(keySafe);
        deposit.setDisposition(disposition);
        
        Token token = tokenService.generateToken(PegasusTokenType.DEPOSIT);
        deposit.setToken(token);
        
        AuthenticatedMember<Member> current = memberService.getCurrent();
        if (current != null) {
            Actor activeActor = current.getActiveActor();
            deposit.setActor(activeActor);
        }
        
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
     * @see org.brekka.pegasus.core.services.InboxService#retrieveById(java.util.UUID)
     */
    @Override
    public Inbox retrieveById(UUID inboxId) {
        Inbox inbox = inboxDAO.retrieveById(inboxId);
        populateNames(Arrays.asList(inbox));
        return inbox;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#retrieveForVault(org.brekka.pegasus.core.model.Vault)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public List<Inbox> retrieveForKeySafe(KeySafe<?> keySafe) {
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
     * @see org.brekka.pegasus.core.services.InboxService#retrieveForDivision(org.brekka.pegasus.core.model.Enlistment)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public List<Inbox> retrieveForDivision(Division<?> division) {
        return inboxDAO.retrieveForDivision(division);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#unlock(org.brekka.pegasus.core.model.Deposit)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Deposit retrieveDeposit(UUID depositId) {
        AccessorContext current = AccessorContextImpl.getCurrent();
        Deposit deposit = current.retrieve(depositId, Deposit.class);
        if (deposit == null) {
            // Need to extract the metadata
            deposit = depositDAO.retrieveById(depositId);
            decryptDocument(deposit);
        } else {
            // Already unlocked, just refresh
            refreshAllocation(deposit);
        }
        return deposit;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#retrieveDeposit(org.brekka.pegasus.core.model.Inbox, java.util.UUID)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public Deposit retrieveDeposit(Inbox inbox, UUID depositId) {
        Deposit deposit = retrieveDeposit(depositId);
        if (deposit == null) {
            return null;
        }
        if (EntityUtils.identityEquals(deposit.getInbox(), inbox)) {
            return deposit;
        }
        throw new PegasusException(PegasusErrorCode.PG745, 
                "The deposit '%s' does not belong to inbox '%s'", depositId, inbox.getId());
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#retrieveForMember()
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public List<Inbox> retrieveForMember() {
        AuthenticatedMember<Member> authenticatedMember = memberService.getCurrent(Member.class);
        List<Inbox> inboxList = inboxDAO.retrieveForMember(authenticatedMember.getMember());
        populateNames(inboxList);
        return inboxList;
    }

    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#retrieveDeposits(org.brekka.pegasus.core.model.Inbox)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public List<Deposit> retrieveDeposits(Inbox inbox, boolean releaseXml) {
        List<Deposit> depositList = depositDAO.retrieveByInbox(inbox);
        if (releaseXml) {
            for (Deposit deposit : depositList) {
                decryptDocument(deposit);
                bindToContext(deposit);
            }
        }
        return depositList;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#deleteDeposit(org.brekka.pegasus.core.model.Deposit)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void deleteDeposit(Deposit deposit) {
        deposit.setExpires(new Date());
        depositDAO.update(deposit);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#retrieveDepositListing(org.brekka.pegasus.core.model.Inbox, org.joda.time.DateTime, org.joda.time.DateTime, boolean, org.brekka.commons.persistence.model.ListingCriteria)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public List<Deposit> retrieveDepositListing(Inbox inbox, DateTime from, DateTime until, boolean showExpired,
            ListingCriteria listingCriteria) {
        List<Deposit> depositList = depositDAO.retrieveListing(inbox, defaultMin(from), defaultMax(until), showExpired, listingCriteria);
        xmlEntityService.releaseAll(depositList, AllocationDocument.class);
        return depositList;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InboxService#retrieveDepositListingRowCount(org.brekka.pegasus.core.model.Inbox, org.joda.time.DateTime, org.joda.time.DateTime, boolean)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public int retrieveDepositListingRowCount(Inbox inbox, DateTime from, DateTime until, boolean showExpired) {
        return depositDAO.retrieveListingRowCount(inbox, defaultMin(from), defaultMax(until), showExpired);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof VaultDeleteEvent) {
            VaultDeleteEvent vaultDeleteEvent = (VaultDeleteEvent) event;
            inboxDAO.deleteWithKeySafe(vaultDeleteEvent.getVault());
        }
    }
    
    private void populateNames(List<Inbox> inboxList) {
        AuthenticatedMember<Member> authenticatedMember = memberService.getCurrent(Member.class);
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
    

    /**
     * @param from
     * @return
     */
    private static DateTime defaultMin(DateTime from) {
        if (from == null) {
            from = new DateTime(0);
        }
        return from;
    }

    /**
     * @param until
     * @return
     */
    private static DateTime defaultMax(DateTime until) {
        if (until == null) {
            until = new DateTime().plusYears(100);
        }
        return until;
    }
}
