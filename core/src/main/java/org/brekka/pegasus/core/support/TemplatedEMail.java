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

import java.util.HashMap;
import java.util.Map;

import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
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
    public TemplatedEMail(String prefix) {
        this.subjectSlug = prefix + "_SUBJECT";
        this.bodySlug = prefix + "_BODY";
    }
    
    public EMailBuilder buildEMail(TemplateService templateService) {
        Template bodyTemplate = retrieveTemplate(templateService, bodySlug);
        Template subjectTemplate = retrieveTemplate(templateService, subjectSlug);
        return new EMailBuilder(templateService, bodyTemplate, subjectTemplate);
    }
    
    protected Template retrieveTemplate(TemplateService templateService, String templateSlug) {
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
        
        private String sender;
        
        /**
         * @param bodyTemplate
         * @param subjectTemplate
         */
        private EMailBuilder(TemplateService templateService, Template bodyTemplate, Template subjectTemplate) {
            this.templateService = templateService;
            this.bodyTemplate = bodyTemplate;
            this.subjectTemplate = subjectTemplate;
        }
        
        /**
         * @param sender the sender to set
         */
        public EMailBuilder setSender(String sender) {
            this.sender = sender;
            return this;
        }
        
        /**
         * Add a value to the context
         * @param key
         * @param value
         */
        public EMailBuilder append(String key, Object value) {
            context.put(key, value);
            return this;
        }
        
        /**
         * Add multiple values
         * @param map
         */
        public EMailBuilder appendAll(Map<String, Object> map) {
            context.putAll(map);
            return this;
        }
        
        public void send(String recipient, EMailSendingService eMailSendingService, KeySafe<?> keySafe) {
            String subject = templateService.merge(subjectTemplate, context);
            String body = templateService.merge(bodyTemplate, context);
            eMailSendingService.send(recipient, sender, subject, body, null, keySafe);
        }
    }
    
}
