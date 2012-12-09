/*
 * Copyright 2012 the original author or authors.
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

import org.brekka.pegasus.core.model.Associate;
import org.brekka.pegasus.core.model.Division;
import org.brekka.pegasus.core.model.DivisionAssociate;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.phalanx.api.model.KeyPair;

/**
 * TODO Description of DivisionService
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface DivisionService {
    DivisionAssociate createRootDivision(Associate associate, Vault connectedTo, String slug, String name);
    
    Division createRootDivision(Organization organization, KeyPair keyPair, String slug, String name);
    
    Division createDivision(Division parent, String slug, String name);
    
    /**
     * @param orgToken
     * @param divisionToken
     * @return
     */
    Division retrieveDivision(Organization organization, String divisionSlug);
    
    /**
     * @param organization
     * @return
     */
    List<DivisionAssociate> retrieveCurrentDivisions();
}
