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

package org.brekka.pegasus.core.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.model.Attachment;
import org.brekka.pegasus.core.model.EMailMessage;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Template;
import org.brekka.pegasus.core.services.EMailSendingService;
import org.brekka.pegasus.core.services.TemplateService;

/**
 * Helps build E-Mails
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class TemplatedEMail {

    private final String subjectSlug;
    private final String bodySlug;

    /**
     * @param prefix
     */
    public TemplatedEMail(final String prefix) {
        this.subjectSlug = prefix + "_SUBJECT";
        this.bodySlug = prefix + "_BODY";
    }

    public EMailBuilder buildEMail(final TemplateService templateService) {
        Template bodyTemplate = retrieveTemplate(templateService, this.bodySlug);
        Template subjectTemplate = retrieveTemplate(templateService, this.subjectSlug);
        return new EMailBuilder(templateService, bodyTemplate, subjectTemplate);
    }

    protected Template retrieveTemplate(final TemplateService templateService, final String templateSlug) {
        Template template = templateService.retrieveBySlug(templateSlug);
        if (template == null) {
            throw new PegasusException(PegasusErrorCode.PG871, "Template '%s' not found", templateSlug);
        }
        return template;
    }


    public class EMailBuilder {
        private final Map<String, Object> context = new HashMap<>();

        private final TemplateService templateService;
        private final Template bodyTemplate;
        private final Template subjectTemplate;
        private final List<Attachment> attachments = new ArrayList<>();

        private String sender;

        /**
         * @param bodyTemplate
         * @param subjectTemplate
         */
        private EMailBuilder(final TemplateService templateService, final Template bodyTemplate, final Template subjectTemplate) {
            this.templateService = templateService;
            this.bodyTemplate = bodyTemplate;
            this.subjectTemplate = subjectTemplate;
        }

        /**
         * @param sender the sender to set
         */
        public EMailBuilder setSender(final String sender) {
            this.sender = sender;
            return this;
        }

        /**
         * Add a value to the context
         * @param key
         * @param value
         */
        public EMailBuilder append(final String key, final Object value) {
            this.context.put(key, value);
            return this;
        }

        /**
         * Add multiple values
         * @param map
         */
        public EMailBuilder appendAll(final Map<String, Object> map) {
            this.context.putAll(map);
            return this;
        }

        public boolean send(final String recipient, final EMailSendingService eMailSendingService, final KeySafe<?> keySafe) {
            return send(Arrays.asList(recipient), eMailSendingService, keySafe);
        }

        public boolean send(final List<String> recipients, final EMailSendingService eMailSendingService, final KeySafe<?> keySafe) {
            String subject = this.templateService.merge(this.subjectTemplate, this.context);
            String body = this.templateService.merge(this.bodyTemplate, this.context);
            EMailMessage mail = eMailSendingService.send(recipients, this.sender, subject, body, null, this.attachments, keySafe);
            return (mail.getReference() != null);
        }

        /**
         * @param string
         * @param inputStreamSource
         * @return
         */
        public EMailBuilder addAttachment(final Attachment attachment) {
            this.attachments.add(attachment);
            return this;
        }
    }

}
