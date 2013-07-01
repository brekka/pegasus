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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.brekka.commons.persistence.model.ListingCriteria;
import org.brekka.pegasus.core.dao.EMailMessageDAO;
import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.EMailAddress;
import org.brekka.pegasus.core.model.EMailMessage;
import org.brekka.pegasus.core.model.EMailRecipient;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.XmlEntity;
import org.brekka.pegasus.core.services.EMailAddressService;
import org.brekka.pegasus.core.services.EMailSendingService;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.services.XmlEntityService;
import org.brekka.xml.pegasus.v2.model.EMailMessageDocument;
import org.brekka.xml.pegasus.v2.model.EMailMessageType;
import org.brekka.xml.pegasus.v2.model.EMailMessageType.Content;
import org.brekka.xml.pegasus.v2.model.EMailType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class of email sending service that persists sent messages to the database.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Transactional
public abstract class AbstractEMailSendingService implements EMailSendingService {

    
    @Autowired
    private MemberService memberService;
    
    @Autowired
    private EMailAddressService eMailAddressService;
    
    @Autowired
    private EMailMessageDAO eMailMessageDAO;
    
    @Autowired
    private XmlEntityService xmlEntityService;
    
    private String defaultSourceAddress;
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.EMailSendingService#send(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    @Transactional()
    public EMailMessage send(String recipient, String sender, String subject, String plainBody, String htmlBody, KeySafe<?> keySafe) {
        return send(Arrays.asList(recipient), sender, subject, plainBody, htmlBody, keySafe);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.EMailSendingService#send(java.util.Collection, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    @Transactional()
    public EMailMessage send(Collection<String> recipients, String sender, String subject, String plainBody,
            String htmlBody, KeySafe<?> keySafe) {
        if (sender == null) {
            sender = defaultSourceAddress;
        }
        
        Member member = null;
        AuthenticatedMember<Member> current = memberService.getCurrent();
        if (current != null) {
            member = current.getMember();
        }

        EMailMessage message = new EMailMessage();
        message.setId(UUID.randomUUID());
        message.setOwner(member);
        message.setSender(toAddress(sender));
        
        List<EMailRecipient> eMailRecipientList = new ArrayList<>();
        for (String recipient : recipients) {
            EMailRecipient eMailRecipient = new EMailRecipient();
            eMailRecipient.setAddress(toAddress(recipient));
            eMailRecipient.setMessage(message);
            eMailRecipientList.add(eMailRecipient);
        }
        message.setRecipients(eMailRecipientList);
        
        EMailMessageDocument document = EMailMessageDocument.Factory.newInstance();
        EMailMessageType eMailMessage = document.addNewEMailMessage();
        EMailType senderXml = eMailMessage.addNewSender();
        populateAddress(senderXml, message.getSender());
        for (EMailRecipient eMailRecipient : eMailRecipientList) {
            eMailRecipient.setId(UUID.randomUUID());
            EMailType recipientXml = eMailMessage.addNewRecipient();
            populateAddress(recipientXml, eMailRecipient.getAddress());
        }
        if (StringUtils.isNotBlank(plainBody)) {
            Content content = eMailMessage.addNewContent();
            content.setType(Content.Type.PLAIN);
            content.setStringValue(plainBody);
        }
        if (StringUtils.isNotBlank(htmlBody)) {
            Content content = eMailMessage.addNewContent();
            content.setType(Content.Type.HTML);
            content.setStringValue(htmlBody);
        }
        eMailMessage.setSubject(subject);
        eMailMessage.setUUID(message.getId().toString());
        
        XmlEntity<EMailMessageDocument> xml;
        if (keySafe == null) {
            xml = xmlEntityService.persistPlainEntity(document, false);
        } else {
            xml = xmlEntityService.persistEncryptedEntity(document, keySafe, false);
        }
        message.setXml(xml);
        
        
        String reference = sendInternal(recipients, sender, subject, plainBody, htmlBody);
        message.setReference(reference);
        eMailMessageDAO.create(message);
        return message;
    }


    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.EMailSendingService#retrieveById(java.util.UUID)
     */
    @Override
    @Transactional(readOnly=true)
    public EMailMessage retrieveById(UUID emailMessageId) {
        return eMailMessageDAO.retrieveById(emailMessageId);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.EMailSendingService#retrieveForRecipient(java.lang.String, org.brekka.commons.persistence.model.ListingCriteria)
     */
    @Override
    @Transactional(readOnly=true)
    public List<EMailMessage> retrieveForRecipient(String recipientAddress, ListingCriteria listingCriteria) {
        EMailAddress eMailAddress = eMailAddressService.retrieveByAddress(recipientAddress);
        List<EMailMessage> eMailList = eMailMessageDAO.retrieveForRecipient(eMailAddress, listingCriteria);
        xmlEntityService.releaseAll(eMailList, EMailMessageDocument.class);
        return eMailList;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.EMailSendingService#retrieveForRecipientRowCount(org.brekka.pegasus.core.model.EMailAddress)
     */
    @Override
    @Transactional(readOnly=true)
    public int retrieveForRecipientRowCount(String recipientAddress) {
        EMailAddress eMailAddress = eMailAddressService.retrieveByAddress(recipientAddress);
        return eMailMessageDAO.retrieveForRecipientRowCount(eMailAddress);
    }
    
    protected abstract String sendInternal(Collection<String> recipients, String sender, String subject, String plainBody, String htmlBody);
    
    
    /**
     * @param sender
     * @return
     */
    private EMailAddress toAddress(String address) {
        EMailAddress eMailAddress = eMailAddressService.retrieveByAddress(address);
        if (eMailAddress == null) {
            eMailAddress = eMailAddressService.createEMail(address, null, false);
        }
        return eMailAddress;
    }
    
    /**
     * @param defaultSourceAddress the defaultSourceAddress to set
     */
    protected void setDefaultSourceAddress(String defaultSourceAddress) {
        this.defaultSourceAddress = defaultSourceAddress;
    }

    /**
     * @param senderXml
     * @param sender
     */
    private static void populateAddress(EMailType xml, EMailAddress address) {
        xml.setAddress(address.getAddress());
        xml.setUUID(address.getId().toString());
    }
}
