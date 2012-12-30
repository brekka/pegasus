/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.List;
import java.util.UUID;

import org.brekka.pegasus.core.dao.InvitationDAO;
import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.Invitation;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.model.XmlEntity;
import org.brekka.pegasus.core.services.InvitationService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.XmlEntityService;
import org.brekka.xml.pegasus.v2.model.InvitationDocument;
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
public class InvitationServiceImpl implements InvitationService {

    @Autowired
    private MemberService memberService;
    
    @Autowired
    private InvitationDAO invitationDAO;
    
    @Autowired
    private XmlEntityService xmlEntityService;
    
    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public Invitation createInvitation(InvitationDocument document, Member recipient, UUID invitedResourceCryptedDataId) {
        Invitation invitation = new Invitation();
        
        AuthenticatedMember<Member> current = memberService.getCurrent(Member.class);
        
        Vault defaultVault = recipient.getDefaultVault();
        XmlEntity<InvitationDocument> xmlEntity = xmlEntityService.persistEncryptedEntity(document, defaultVault, false);
        
        invitation.setXml(xmlEntity);
        invitation.setRecipient(recipient);
        invitation.setSender(current.getActiveActor());
        invitation.setCryptedDataId(invitedResourceCryptedDataId);
        
        invitationDAO.create(invitation);
        
        return invitation;
    }
    
    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public List<Invitation> retrieveCurrent(Vault vault) {
        AuthenticatedMember<Member> current = memberService.getCurrent(Member.class);
        return invitationDAO.retrieveForVault(current.getMember(), vault);
    }
}
