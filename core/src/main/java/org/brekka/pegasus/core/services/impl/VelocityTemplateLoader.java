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

import java.io.Reader;
import java.io.StringReader;
import java.util.UUID;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.util.ExtProperties;
import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.dao.TemplateDAO;
import org.brekka.pegasus.core.model.Template;
import org.brekka.pegasus.core.services.XmlEntityService;
import org.brekka.xml.pegasus.v2.model.TemplateDocument;
import org.brekka.xml.pegasus.v2.model.TemplateType;

/**
 * Load templates for velocity. Not autowired as Template engines are optional.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class VelocityTemplateLoader extends ResourceLoader {

    private TemplateDAO templateDAO;

    private XmlEntityService xmlEntityService;


    @Override
    public void init(final ExtProperties configuration) {
        this.templateDAO = (TemplateDAO) this.rsvc.getApplicationAttribute(TemplateDAO.class.getName());
        this.xmlEntityService = (XmlEntityService) this.rsvc.getApplicationAttribute(XmlEntityService.class.getName());
    }

    @Override
    public Reader getResourceReader(final String tempateIdStr, final String encoding) throws ResourceNotFoundException {
        Template template = findTemplate(tempateIdStr);
        this.xmlEntityService.release(template, TemplateDocument.class);
        TemplateDocument templateDocument = template.getXml().getBean();
        TemplateType templateType = templateDocument.getTemplate();
        String content = templateType.getContent();
        return new StringReader(content);
    }

    @Override
    public boolean isSourceModified(final Resource resource) {
        long cachedLastModified = resource.getLastModified();
        long currentLastModified = getLastModified(resource);
        boolean modified = currentLastModified != cachedLastModified;
        return modified;
    }

    @Override
    public long getLastModified(final Resource resource) {
        Template template = findTemplate(resource.getName());
        long lastModified = template.getXml().getCreated().getTime();
        return lastModified;
    }

    protected Template findTemplate(final String resourceKey) {
        Template template;
        try {
            UUID templateId = UUID.fromString(resourceKey);
            template = this.templateDAO.retrieveById(templateId);
        } catch (IllegalArgumentException e) {
            // Not a UUID, try slug
            template = this.templateDAO.retrieveBySlug(resourceKey);
        }
        if (template == null) {
            throw new PegasusException(PegasusErrorCode.PG107,
                    "Template not found key resource key '%s'. Only ids or slugs can be used to lookup templates.", resourceKey);
        }
        return template;
    }
}
