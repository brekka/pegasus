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

package org.brekka.pegasus.core.services;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.model.ListingCriteria;
import org.brekka.pegasus.core.model.Attachment;
import org.brekka.pegasus.core.model.EMailAddress;
import org.brekka.pegasus.core.model.EMailMessage;
import org.brekka.pegasus.core.model.KeySafe;

/**
 * Send an e-mail
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface EMailSendingService {

    /**
     * Send an e-mail to a single recipient
     * 
     * @param recipient who should receive the message
     * @param sender the sender of the message.
     * @param subject what the e-mail is about
     * @param plainBody the plain body (optional)
     * @param htmlBody the html body (optional).
     */
    EMailMessage send(String recipient, String sender, String subject, String plainBody, String htmlBody, KeySafe<?> keySafe);
    
    
    /**
     * Send an e-mail to a multiple recipients
     * 
     * @param recipients who should receive the message
     * @param sender the sender of the message.
     * @param subject what the e-mail is about
     * @param plainBody the plain body (optional)
     * @param htmlBody the html body (optional).
     */
    EMailMessage send(Collection<String> recipients, String sender, String subject, String plainBody, String htmlBody, KeySafe<?> keySafe);
    
    /**
     * Send an e-mail to a multiple recipients with plain attachments
     * 
     * @param recipients who should receive the message
     * @param sender the sender of the message.
     * @param subject what the e-mail is about
     * @param plainBody the plain body (optional)
     * @param htmlBody the html body (optional).
     */
    EMailMessage send(Collection<String> recipients, String sender, String subject, String plainBody, String htmlBody, List<Attachment> attachments, KeySafe<?> keySafe);
    
    /**
     * Retrieve a sent message by its ID
     * @param emailMessageId
     * @return
     */
    EMailMessage retrieveById(UUID emailMessageId);
    
    /**
     * Retrieve the list of e-mail addresses for the specified recipient address.
     * @param recipientAddress
     * @return
     */
    List<EMailMessage> retrieveForRecipient(String recipientAddress, ListingCriteria listingCriteria);
    
    int retrieveForRecipientRowCount(String recipientAddress);
}
