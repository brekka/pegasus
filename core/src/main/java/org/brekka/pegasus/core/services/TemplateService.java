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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.brekka.commons.persistence.model.ListingCriteria;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Template;
import org.brekka.pegasus.core.model.TemplateEngine;
import org.brekka.pegasus.core.model.Token;
import org.brekka.xml.pegasus.v2.model.TemplateType;

/**
 * The template service
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface TemplateService {

    /**
     * Merge the variables in the specified context into the template to produce the resulting string.
     * 
     * @param template
     *            the template to merge values into
     * @param context
     *            contains the variables to merge into the template.
     * @return the result of substituting the variables into the template. If the template is blank, null will be
     *         returned
     */
    @Nullable
    String merge(@Nonnull Template template, @Nonnull Map<String, Object> context);
    
    /**
     * Perform a dynamic render of the specified template string for the purpose of previewing. Naturally this will be quite
     * slow and so should be used sparingly.
     * 
     * @param templateContent
     * @param templateEngine
     * @param context
     * @return
     */
    @Nullable
    String preview(@Nonnull String templateContent, @Nonnull TemplateEngine templateEngine, @Nonnull Map<String, Object> context);

    /**
     * Retrieve a template by token
     * 
     * @param token
     * @return the template or null if it cannot be found
     */
    @Nullable
    Template retrieveByToken(@Nonnull Token token);

    /**
     * Retrieve a template by its slug.
     * 
     * @param slug
     * @return the template or null if it cannot be found
     */
    @Nullable
    Template retrieveBySlug(@Nonnull String slug);

    /**
     * Retrieve a template via its unique id.
     * 
     * @param templateId
     * @return the template or null if it cannot be found
     */
    @Nullable
    Template retrieveById(@Nonnull UUID templateId);

    /**
     * Create a template with the specified details and optionally a slug and/or token.
     * 
     * @param details
     *            the details of this template (documentation, content, engine type).
     * @param engine
     *            the template engine in use.
     * @param keySafe
     *            if specified, will be used to encrypt the template. Otherwise template will be plaintext.
     * @param slug
     *            the slug to identify this template (optional)
     * @param token
     *            the token to identify this template (optional)
     * @param label
     *            optional plaintext label to assign (useful for listing).
     * 
     * @return the newly created template
     */
    @Nonnull
    Template create(@Nonnull TemplateType details, @Nonnull TemplateEngine engine, @Nullable KeySafe<?> keySafe, @Nullable String slug,
            @Nullable Token token, @Nullable String label);

    /**
     * Update the specified template. Changes to the XML should be sure to include the version number.
     * 
     * @param template
     *            the template to update
     */
    void update(@Nonnull Template template);

    /**
     * Delete the template with the specified id.
     * 
     * @param templateId
     *            if of the template to remove.
     */
    void delete(UUID templateId);
    
    /**
     * Retrieve the set of available engines
     * @return the engine set.
     */
    Set<TemplateEngine> getAvailableEngines();

    /**
     * @return
     */
    int retrieveListingRowCount();

    /**
     * @param listingCriteria
     * @return
     */
    List<Template> retrieveListing(ListingCriteria listingCriteria);
}
