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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.dao.TemplateDAO;
import org.brekka.pegasus.core.model.Template;
import org.brekka.pegasus.core.services.XmlEntityService;

/**
 * Velocity based template engine.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class VelocityTemplateEngine implements TemplateEngineAdapter {

    public static final String NAME = "pegasus";

    public static final String RESOURCE_LOADER_CLASS = "pegasus.resource.loader.class";

    public static final String RESOURCE_LOADER_CACHE = "pegasus.resource.loader.cache";

    private VelocityEngine velocityEngine;

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.impl.TemplateEngineAdapter#init(org.brekka.pegasus.core.dao.TemplateDAO, org.brekka.pegasus.core.services.XmlEntityService)
     */
    @Override
    public void init(final TemplateDAO templateDAO, final XmlEntityService xmlEntityService) {
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, NAME);
        velocityEngine.setProperty(RuntimeConstants.VM_LIBRARY, "");
        velocityEngine.setProperty(RESOURCE_LOADER_CLASS, VelocityTemplateLoader.class.getName());
        // Disable caching as it seems a bit shit
        velocityEngine.setProperty(RESOURCE_LOADER_CACHE, Boolean.FALSE.toString());
        velocityEngine.setProperty("velocimacro.permissions.allow.inline.local.scope", Boolean.TRUE.toString());

        velocityEngine.setApplicationAttribute(TemplateDAO.class.getName(), templateDAO);
        velocityEngine.setApplicationAttribute(XmlEntityService.class.getName(), xmlEntityService);
        velocityEngine.init();
        this.velocityEngine = velocityEngine;
    }

    /**
     * @param template
     * @param context
     * @return
     */
    @Override
    public void merge(final Template template, final Map<String, Object> context, final Writer out) {
        String templateName = template.getId().toString();
        VelocityContext velocityContext = new VelocityContext(context);
        if (!this.velocityEngine.mergeTemplate(templateName, "UTF-8", velocityContext, out)) {
            throw new PegasusException(PegasusErrorCode.PG431,
                    "Velocity failed to merge template '%s' with context %s", templateName, context);
        }
    }

    @Override
    public void preview(final String templateContent, final Map<String, Object> context, final Writer out) {
        VelocityContext velocityContext = new VelocityContext(context);
        try (Reader reader = new StringReader(templateContent)) {
            if (!this.velocityEngine.evaluate(velocityContext, out, "Dynamic", reader)) {
                throw new PegasusException(PegasusErrorCode.PG431,
                        "Velocity preview failed to merge dynamic template with context");
            }
        } catch (IOException e) {
            throw new PegasusException(PegasusErrorCode.PG453, e,
                    "Error from velocity preview merging template with context");
        }

    }

}
