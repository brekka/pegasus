/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.List;

import org.brekka.pegasus.core.dao.InvitationDAO;
import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.Invitation;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.model.XmlEntity;
import org.brekka.pegasus.core.services.InvitationService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.XmlEntityService;
import org.brekka.xml.pegasus.v1.model.InvitationDocument;
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
    public Invitation createInvitation(InvitationDocument document, Member recipient) {
        Invitation invitation = new Invitation();
        
        AuthenticatedMember current = memberService.getCurrent();
        
        Vault defaultVault = recipient.getDefaultVault();
        XmlEntity<InvitationDocument> xmlEntity = xmlEntityService.persistEncryptedEntity(document, defaultVault);
        
        invitation.setXml(xmlEntity);
        invitation.setRecipient(recipient);
        invitation.setSender(current.getActiveActor());
        
        invitationDAO.create(invitation);
        
        return invitation;
    }
    
    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public List<Invitation> retrieveCurrent(Vault vault) {
        AuthenticatedMember current = memberService.getCurrent();
        return invitationDAO.retrieveForVault(current.getMember(), vault);
    }
}
