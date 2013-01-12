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

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;

import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.dao.TemplateDAO;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Template;
import org.brekka.pegasus.core.model.TemplateEngine;
import org.brekka.pegasus.core.model.Token;
import org.brekka.pegasus.core.model.XmlEntity;
import org.brekka.pegasus.core.services.TemplateService;
import org.brekka.pegasus.core.services.XmlEntityService;
import org.brekka.xml.pegasus.v2.model.TemplateDocument;
import org.brekka.xml.pegasus.v2.model.TemplateType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ClassUtils;

/**
 * Standard implementation of the template service.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Service
@Transactional
public class TemplateServiceImpl implements TemplateService {
    
    @Autowired
    private TemplateDAO templateDAO;
    
    @Autowired
    private XmlEntityService xmlEntityService;
    
    
    public Map<TemplateEngine, TemplateEngineAdapter> adapters;
    
    @PostConstruct
    public void init() {
        if (adapters != null) {
            // externally configured
            return;
        }
        Map<TemplateEngine, TemplateEngineAdapter> adapters = new LinkedHashMap<>();
        if (ClassUtils.isPresent("org.apache.velocity.app.VelocityEngine", getClass().getClassLoader())) {
            // Velocity is present, add it
            TemplateEngineAdapter templateEngineAdapter = new org.brekka.pegasus.core.services.impl.VelocityTemplateEngine();
            templateEngineAdapter.init(templateDAO, xmlEntityService);
            adapters.put(TemplateEngine.VELOCITY, templateEngineAdapter);
        }
        this.adapters = adapters;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.TemplateService#merge(org.brekka.pegasus.core.model.Template, java.util.Map)
     */
    @Override
    @Nullable
    @Transactional(propagation=Propagation.REQUIRED)
    public String merge(@Nonnull Template template, @Nonnull Map<String, Object> context) {
        String merged;
        TemplateEngineAdapter templateEngineAdapter = adapters.get(template.getEngine());
        if (templateEngineAdapter != null) {
            merged = templateEngineAdapter.merge(template, context);
        } else {
            throw new PegasusException(PegasusErrorCode.PG773, 
                    "No logic configured for engine '%s'", template.getEngine());
        }
        return merged;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.TemplateService#preview(java.lang.String, org.brekka.pegasus.core.model.TemplateEngine, java.util.Map)
     */
    @Override
    @Nullable
    public String preview(String templateContent, TemplateEngine templateEngine, Map<String, Object> context) {
        String merged;
        TemplateEngineAdapter templateEngineAdapter = adapters.get(templateEngine);
        if (templateEngineAdapter != null) {
            merged = templateEngineAdapter.preview(templateContent, context);
        } else {
            throw new PegasusException(PegasusErrorCode.PG773, 
                    "No logic configured for engine '%s'", templateEngine);
        }
        return merged;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.TemplateService#retrieveByToken(org.brekka.pegasus.core.model.Token)
     */
    @Override
    @Nullable
    @Transactional(propagation=Propagation.REQUIRED)
    public Template retrieveByToken(@Nonnull Token token) {
        return templateDAO.retrieveByToken(token);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.TemplateService#retrieveBySlug(java.lang.String)
     */
    @Override
    @Nullable
    @Transactional(propagation=Propagation.REQUIRED)
    public Template retrieveBySlug(@Nonnull String slug) {
        return templateDAO.retrieveBySlug(slug);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.TemplateService#retrieveById(java.util.UUID)
     */
    @Override
    @Nullable
    @Transactional(propagation=Propagation.REQUIRED)
    public Template retrieveById(@Nonnull UUID templateId) {
        return templateDAO.retrieveById(templateId);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.TemplateService#create(org.brekka.xml.pegasus.v2.model.TemplateType, java.lang.String, org.brekka.pegasus.core.model.Token)
     */
    @Override
    @Nonnull
    @Transactional(propagation = Propagation.REQUIRED)
    public Template create(@Nonnull TemplateType details, @Nonnull TemplateEngine engine, @Nullable KeySafe<?> keySafe,
            @Nullable String slug, @Nullable Token token, @Nullable String label) {
        Template template = new Template();
        TemplateDocument templateDocument = TemplateDocument.Factory.newInstance();
        templateDocument.setTemplate(details);
        
        XmlEntity<TemplateDocument> xml;
        if (keySafe == null) {
            xml = xmlEntityService.persistPlainEntity(templateDocument, false);
        } else {
            xml = xmlEntityService.persistEncryptedEntity(templateDocument, keySafe, false);
        }
        template.setEngine(engine);
        template.setSlug(slug);
        template.setToken(token);
        template.setXml(xml);
        template.setLabel(label);
        templateDAO.create(template);
        return template;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.TemplateService#update(org.brekka.pegasus.core.model.Template)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void update(@Nonnull Template template) {
        Template managed = templateDAO.retrieveById(template.getId());
        
        XmlEntity<TemplateDocument> incoming = template.getXml();
        XmlEntity<TemplateDocument> current = managed.getXml();
        XmlEntity<TemplateDocument> xml = xmlEntityService.updateEntity(incoming, current, TemplateDocument.class);
        managed.setXml(xml);
        // Allow the caller to update the slug/token.
        managed.setSlug(template.getSlug());
        managed.setToken(template.getToken());
        managed.setEngine(template.getEngine());
        managed.setLabel(template.getLabel());
        
        templateDAO.update(managed);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.TemplateService#delete(java.util.UUID)
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED)
    public void delete(UUID templateId) {
        templateDAO.delete(templateId);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.TemplateService#getAvailableEngines()
     */
    @Override
    public Set<TemplateEngine> getAvailableEngines() {
        Set<TemplateEngine> engines = EnumSet.copyOf(adapters.keySet());
        return engines;
    }
    
    /**
     * @param adapters the adapters to set
     */
    public void setAdapters(Map<TemplateEngine, TemplateEngineAdapter> adapters) {
        this.adapters = adapters;
    }
}
