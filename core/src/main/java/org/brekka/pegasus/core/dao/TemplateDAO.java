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

package org.brekka.pegasus.core.dao;

import java.util.List;
import java.util.UUID;

import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.commons.persistence.model.ListingCriteria;
import org.brekka.pegasus.core.model.Template;
import org.brekka.pegasus.core.model.Token;
import org.joda.time.DateTime;

/**
 * Manipulate Template objects
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface TemplateDAO extends EntityDAO<UUID, Template> {

    Template retrieveByToken(Token token);

    Template retrieveBySlug(String slug);

    /**
     * @return
     */
    int retrieveListingRowCount();

    /**
     * @param listingCriteria
     * @return
     */
    List<Template> retrieveListing(DateTime changedSince, ListingCriteria listingCriteria);
}
