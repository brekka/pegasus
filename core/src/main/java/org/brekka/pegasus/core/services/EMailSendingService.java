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
    void send(String recipient, String sender, String subject, String plainBody, String htmlBody);
    
    /**
     * Send an e-mail to a multiple recipients
     * 
     * @param recipients who should receive the message
     * @param sender the sender of the message.
     * @param subject what the e-mail is about
     * @param plainBody the plain body (optional)
     * @param htmlBody the html body (optional).
     */
    void send(Collection<String> recipients, String sender, String subject, String plainBody, String htmlBody);
}
