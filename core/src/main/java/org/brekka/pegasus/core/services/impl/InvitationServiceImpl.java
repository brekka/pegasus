/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.brekka.pegasus.core.services.impl;

import java.util.List;
import java.util.UUID;

import org.brekka.pegasus.core.dao.InvitationDAO;
import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.Invitation;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.PegasusTokenType;
import org.brekka.pegasus.core.model.Token;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.model.XmlEntity;
import org.brekka.pegasus.core.services.InvitationService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.TokenService;
import org.brekka.pegasus.core.services.XmlEntityService;
import org.brekka.xml.pegasus.v2.model.InvitationDocument;
import org.brekka.xml.pegasus.v2.model.InvitationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Create invitations
 *
 * @author Andrew Taylor (andrew@brekka.org)
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
    
    @Autowired
    private TokenService tokenService;
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InvitationService#createInvitation(org.brekka.xml.pegasus.v2.model.InvitationType, java.lang.String)
     */
    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public Invitation createInvitation(InvitationType details, String password) {
        return createInvitation(details, null, password);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InvitationService#createInvitation(org.brekka.xml.pegasus.v2.model.InvitationType, org.brekka.pegasus.core.model.Member)
     */
    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public Invitation createInvitation(InvitationType details, Member recipient) {
        return createInvitation(details, recipient, null);
    }
    
    
    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public List<Invitation> retrieveForMember(Member member) {
        AuthenticatedMember<Member> current = memberService.getCurrent(Member.class);
        return invitationDAO.retrieveForMember(current.getMember());
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InvitationService#retrieveByToken(org.brekka.pegasus.core.model.Token)
     */
    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public Invitation retrieveByToken(Token token) {
        return invitationDAO.retrieveByToken(token);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InvitationService#update(org.brekka.pegasus.core.model.Invitation)
     */
    @Transactional(propagation=Propagation.REQUIRED)
    @Override
    public void update(Invitation invitation) {
        Invitation managed = invitationDAO.retrieveById(invitation.getId());
        managed.setStatus(invitation.getStatus());
        managed.setActioned(invitation.getActioned());
        if (managed.getRecipient() != null) {
            XmlEntity<InvitationDocument> xml = xmlEntityService.updateEntity(invitation.getXml(), managed.getXml(), InvitationDocument.class);
            managed.setXml(xml);
        }
        invitationDAO.update(managed);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.InvitationService#retrieveById(java.util.UUID)
     */
    @Override
    public Invitation retrieveById(UUID invitationId) {
        return invitationDAO.retrieveById(invitationId);
    }

    
    protected Invitation createInvitation(InvitationType invitationType, Member recipient, String password) {
        Invitation invitation = new Invitation();
        
        InvitationDocument invitationDocument = InvitationDocument.Factory.newInstance();
        invitationDocument.setInvitation(invitationType);
        
        XmlEntity<InvitationDocument> xmlEntity;
        if (recipient != null) {
            Vault defaultVault = recipient.getDefaultVault();
            xmlEntity = xmlEntityService.persistEncryptedEntity(invitationDocument, defaultVault, false);
        } else {
            xmlEntity = xmlEntityService.persistEncryptedEntity(invitationDocument, password, false);
        }
        
        invitation.setXml(xmlEntity);
        invitation.setRecipient(recipient);
        
        AuthenticatedMember<Member> current = memberService.getCurrent(Member.class);
        invitation.setSender(current.getActiveActor());
        
        Token token = tokenService.generateToken(PegasusTokenType.INVITATION);
        invitation.setToken(token);
        
        invitationDAO.create(invitation);
        
        return invitation;
    }
}
