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

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.brekka.commons.persistence.model.ListingCriteria;
import org.brekka.commons.persistence.model.OrderByPart;
import org.brekka.commons.persistence.model.OrderByProperty;
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
import org.brekka.xml.pegasus.v2.model.ExportedTemplateType;
import org.brekka.xml.pegasus.v2.model.ExportedTemplatesDocument;
import org.brekka.xml.pegasus.v2.model.ExportedTemplatesDocument.ExportedTemplates;
import org.brekka.xml.pegasus.v2.model.TemplateDocument;
import org.brekka.xml.pegasus.v2.model.TemplateType;
import org.joda.time.DateTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ClassUtils;

/**
 * Standard implementation of the template service.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Service
@Transactional
public class TemplateServiceImpl implements TemplateService, InitializingBean {

    @Autowired
    private TemplateDAO templateDAO;

    @Autowired
    private XmlEntityService xmlEntityService;


    public Map<TemplateEngine, TemplateEngineAdapter> adapters;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.adapters != null) {
            // externally configured
            return;
        }
        Map<TemplateEngine, TemplateEngineAdapter> templateAdapters = new LinkedHashMap<>();
        if (ClassUtils.isPresent("org.apache.velocity.app.VelocityEngine", getClass().getClassLoader())) {
            // Velocity is present, add it
            TemplateEngineAdapter templateEngineAdapter = new org.brekka.pegasus.core.services.impl.VelocityTemplateEngine();
            templateEngineAdapter.init(this.templateDAO, this.xmlEntityService);
            templateAdapters.put(TemplateEngine.VELOCITY, templateEngineAdapter);
        }
        this.adapters = templateAdapters;
    }

    @Override
    public String merge(final Template template, final Map<String, Object> context) {
        StringWriter writer = new StringWriter();
        merge(template, context, writer);
        return writer.toString();
    }

    @Override
    public void merge(final Template template, final Map<String, Object> context, final Writer out) {
        TemplateEngineAdapter templateEngineAdapter = this.adapters.get(template.getEngine());
        if (templateEngineAdapter != null) {
            templateEngineAdapter.merge(template, context, out);
        } else {
            throw new PegasusException(PegasusErrorCode.PG773,
                    "No logic configured for engine '%s'", template.getEngine());
        }
    }

    @Override
    public String preview(final String templateContent, final TemplateEngine templateEngine, final Map<String, Object> context) {
        StringWriter writer = new StringWriter();
        preview(templateContent, templateEngine, context, writer);
        return writer.toString();
    }

    @Override
    public void preview(final String templateContent, final TemplateEngine templateEngine, final Map<String, Object> context, final Writer out) {
        TemplateEngineAdapter templateEngineAdapter = this.adapters.get(templateEngine);
        if (templateEngineAdapter != null) {
            templateEngineAdapter.preview(templateContent, context, out);
        } else {
            throw new PegasusException(PegasusErrorCode.PG773,
                    "No logic configured for engine '%s'", templateEngine);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public Template retrieveByToken(final Token token) {
        return this.templateDAO.retrieveByToken(token);
    }

    @Override
    @Transactional(readOnly=true)
    public Template retrieveBySlug(final String slug) {
        return this.templateDAO.retrieveBySlug(slug);
    }

    @Override
    @Transactional(readOnly=true)
    public Template retrieveById(final UUID templateId) {
        return this.templateDAO.retrieveById(templateId);
    }

    @Override
    @Transactional()
    public Template create(final TemplateType details, final TemplateEngine engine, final KeySafe<?> keySafe,
            final String slug, final Token token, final String label) {
        return create(details, engine, keySafe, slug, token, label, false);
    }

    @Override
    @Transactional(isolation=Isolation.SERIALIZABLE)
    public void update(final Template template) {
        Template managed = this.templateDAO.retrieveById(template.getId());
        fixDetails(template.getXml().getBean().getTemplate());
        XmlEntity<TemplateDocument> incoming = template.getXml();
        XmlEntity<TemplateDocument> current = managed.getXml();
        XmlEntity<TemplateDocument> xml = this.xmlEntityService.updateEntity(incoming, current, TemplateDocument.class);
        managed.setXml(xml);
        // Allow the caller to update the slug/token.
        managed.setSlug(template.getSlug());
        managed.setToken(template.getToken());
        managed.setEngine(template.getEngine());
        managed.setLabel(template.getLabel());
        managed.setImported(template.getImported());

        this.templateDAO.update(managed);
        template.setXml(xml);
    }

    @Override
    @Transactional()
    public void delete(final UUID templateId) {
        this.templateDAO.delete(templateId);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Template> retrieveListing(final ListingCriteria listingCriteria) {
        return this.templateDAO.retrieveListing(null, listingCriteria);
    }

    @Override
    @Transactional(readOnly=true)
    public int retrieveListingRowCount() {
        return this.templateDAO.retrieveListingRowCount();
    }

    @Override
    @Transactional(readOnly=true)
    public ExportedTemplatesDocument exportAll(final DateTime changedSince) {
        int count = this.templateDAO.retrieveListingRowCount();
        List<Template> listing = this.templateDAO.retrieveListing(changedSince, new ListingCriteria(0, count, Arrays.<OrderByPart>asList(new OrderByProperty("created", false))));
        ExportedTemplatesDocument doc = ExportedTemplatesDocument.Factory.newInstance();
        ExportedTemplates templates = doc.addNewExportedTemplates();
        for (Template template : listing) {
            this.xmlEntityService.release(template, TemplateDocument.class);
            TemplateType templateXml = template.getXml().getBean().getTemplate();
            ExportedTemplateType exportedTemplate = templates.addNewExportedTemplate();
            exportedTemplate.setSlug(template.getSlug());
            exportedTemplate.setPlainLabel(template.getLabel());
            exportedTemplate.setLabel(templateXml.getLabel());
            exportedTemplate.setContent(templateXml.getContent());
            exportedTemplate.setContentType(templateXml.getContentType());
            exportedTemplate.setDocumentation(templateXml.getDocumentation());
            exportedTemplate.setEngine(template.getEngine().toString());
            exportedTemplate.setExampleVariables(templateXml.getExampleVariables());
            exportedTemplate.setEncrypt(template.getXml().getCryptedDataId() != null);
        }
        return doc;
    }

    @Override
    @Transactional()
    public int importFrom(final ExportedTemplatesDocument exportedTemplatesDocument, final KeySafe<?> keySafe, final boolean forceUpdate) {
        int count = 0;
        ExportedTemplates exportedTemplates = exportedTemplatesDocument.getExportedTemplates();
        List<ExportedTemplateType> exportedTemplateList = exportedTemplates.getExportedTemplateList();
        for (ExportedTemplateType exportedTemplateType : exportedTemplateList) {
            String slug = exportedTemplateType.getSlug();
            TemplateEngine templateEngine = TemplateEngine.valueOf(exportedTemplateType.getEngine());
            Template existing = this.templateDAO.retrieveBySlug(slug);
            if (existing == null) {
                KeySafe<?> keySafeForCreate = (exportedTemplateType.getEncrypt() ? keySafe : null);
                TemplateType template = TemplateType.Factory.newInstance();
                template.setLabel(exportedTemplateType.getLabel());
                template.setContent(exportedTemplateType.getContent());
                template.setDocumentation(exportedTemplateType.getDocumentation());
                template.setExampleVariables(exportedTemplateType.getExampleVariables());
                if (exportedTemplateType.isSetContentType()) {
                    template.setContentType(exportedTemplateType.getContentType());
                }
                create(template, templateEngine, keySafeForCreate, slug, null, exportedTemplateType.getPlainLabel(), true);
                count++;
            } else if (BooleanUtils.isTrue(existing.getImported()) || forceUpdate) {
                // The template was originally imported or force has been authorized, we can update it.
                if (existing.getXml().getCryptedDataId() != null && !forceUpdate) {
                    // Difficult to know at this point whether we can decrypt the XML. Really need a
                    // XmlEntityService release method that will not throw an exception and rollback our transaction.
                    // Assume 'forceUpdate' ignores this.
                    continue;
                }
                existing.setEngine(templateEngine);
                existing.setLabel(exportedTemplateType.getLabel());
                // If we force updated, treat it as not automatic import.
                existing.setImported(Boolean.valueOf(!forceUpdate));
                this.xmlEntityService.release(existing, TemplateDocument.class);
                XmlEntity<TemplateDocument> xml = existing.getXml();
                TemplateType newXml = xml.getBean().getTemplate();
                newXml.setContent(exportedTemplateType.getContent());
                if (exportedTemplateType.isSetContentType()) {
                    newXml.setContentType(exportedTemplateType.getContentType());
                }
                if (exportedTemplateType.isSetDocumentation()) {
                    newXml.setDocumentation(exportedTemplateType.getDocumentation());
                }
                newXml.setExampleVariables(exportedTemplateType.getExampleVariables());
                newXml.setLabel(exportedTemplateType.getLabel());
                update(existing);
                if (xml.getVersion() != existing.getXml().getVersion()) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public Set<TemplateEngine> getAvailableEngines() {
        Set<TemplateEngine> engines = EnumSet.copyOf(this.adapters.keySet());
        return engines;
    }

    protected Template create(final TemplateType details, final TemplateEngine engine, final KeySafe<?> keySafe,
            final String slug, final Token token, final String label, final boolean imported) {
        Template template = new Template();

        fixDetails(details);
        TemplateDocument templateDocument = TemplateDocument.Factory.newInstance();
        templateDocument.setTemplate(details);

        XmlEntity<TemplateDocument> xml;
        if (keySafe == null) {
            xml = this.xmlEntityService.persistPlainEntity(templateDocument);
        } else {
            xml = this.xmlEntityService.persistEncryptedEntity(templateDocument, keySafe);
        }
        template.setEngine(engine);
        template.setSlug(slug);
        template.setToken(token);
        template.setXml(xml);
        template.setLabel(label);
        template.setImported(imported);
        this.templateDAO.create(template);
        return template;
    }

    @Override
    @Transactional
    public void applyEncryption(final UUID templateId, final KeySafe<?> keySafe) {
        Template managed = this.templateDAO.retrieveById(templateId);
        if (managed.getXml().getCryptedDataId() != null) {
            // Already encrypted
            return;
        }
        XmlEntity<TemplateDocument> xml = this.xmlEntityService.applyEncryption(managed.getXml(), keySafe, TemplateDocument.class);
        managed.setXml(xml);
        this.templateDAO.update(managed);
    }

    @Override
    @Transactional
    public void removeEncryption(final UUID templateId) {
        Template managed = this.templateDAO.retrieveById(templateId);
        if (managed.getXml().getCryptedDataId() == null) {
            // Already plain
            return;
        }
        XmlEntity<TemplateDocument> xml = this.xmlEntityService.removeEncryption(managed.getXml(), TemplateDocument.class);
        managed.setXml(xml);
        this.templateDAO.update(managed);
    }

    protected void fixDetails(final TemplateType details) {
        if (details == null) {
            return;
        }
        if (details.isSetDocumentation()
                && details.getDocumentation() == null) {
            details.unsetDocumentation();
        }
        if (details.getContent() == null) {
            details.setContent(StringUtils.EMPTY);
        }
    }

    /**
     * @param adapters the adapters to set
     */
    public void setAdapters(final Map<TemplateEngine, TemplateEngineAdapter> adapters) {
        this.adapters = adapters;
    }
}
