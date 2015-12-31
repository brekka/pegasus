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

import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.dao.InvitationDAO;
import org.brekka.pegasus.core.model.Invitation;
import org.brekka.pegasus.core.model.InvitationStatus;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.MemberContext;
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
import org.springframework.transaction.annotation.Isolation;
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

    @Transactional()
    @Override
    public Invitation createInvitation(final Token token, final InvitationType details, final String password) {
        return createInvitation(token, details, null, password);
    }

    @Transactional()
    @Override
    public Invitation createInvitation(final Token token, final InvitationType details, final Member recipient) {
        return createInvitation(token, details, recipient, null);
    }


    @Transactional(readOnly=true)
    @Override
    public List<Invitation> retrieveForMember(final Member member) {
        MemberContext current = this.memberService.getCurrent();
        return this.invitationDAO.retrieveForMember(current.getMember());
    }

    @Transactional(readOnly=true)
    @Override
    public Invitation retrieveByToken(final Token token) {
        return retrieveByToken(token, null, null);
    }

    @Override
    @Transactional(readOnly=true)
    public Invitation retrieveByToken(final Token token, final String password, final InvitationStatus requiredStatus) {
        Invitation invitation = this.invitationDAO.retrieveByToken(token);
        if (invitation == null) {
            return null;
        }
        if (invitation.getStatus() != requiredStatus) {
            return null;
        }
        if (password != null) {
            XmlEntity<InvitationDocument> xml;
            try {
                xml = this.xmlEntityService.release(invitation.getXml(), password, InvitationDocument.class);
                invitation.setXml(xml);
            } catch (PegasusException e) {
                return null;
            }
        }
        return invitation;
    }

    @Transactional(isolation=Isolation.SERIALIZABLE)
    @Override
    public void update(final Invitation invitation) {
        Invitation managed = this.invitationDAO.retrieveById(invitation.getId());
        managed.setStatus(invitation.getStatus());
        managed.setActioned(invitation.getActioned());
        if (managed.getRecipient() != null) {
            XmlEntity<InvitationDocument> xml = this.xmlEntityService.updateEntity(invitation.getXml(), managed.getXml(), InvitationDocument.class);
            managed.setXml(xml);
        }
        this.invitationDAO.update(managed);
    }

    @Transactional(readOnly=true)
    @Override
    public Invitation retrieveById(final UUID invitationId) {
        return this.invitationDAO.retrieveById(invitationId);
    }


    protected Invitation createInvitation(Token token, final InvitationType invitationType, final Member recipient, final String password) {
        Invitation invitation = new Invitation();

        InvitationDocument invitationDocument = InvitationDocument.Factory.newInstance();
        invitationDocument.setInvitation(invitationType);

        XmlEntity<InvitationDocument> xmlEntity;
        if (recipient != null) {
            Vault defaultVault = recipient.getDefaultVault();
            xmlEntity = this.xmlEntityService.persistEncryptedEntity(invitationDocument, defaultVault, false);
        } else {
            xmlEntity = this.xmlEntityService.persistEncryptedEntity(invitationDocument, password, false);
        }

        invitation.setXml(xmlEntity);
        invitation.setRecipient(recipient);

        MemberContext current = memberService.getCurrent();
        if (current != null) {
            invitation.setSender(current.getActiveActor());
        }

        if (token == null) {
            token = this.tokenService.generateToken(PegasusTokenType.INVITATION);
        }
        invitation.setToken(token);

        this.invitationDAO.create(invitation);

        return invitation;
    }
}
