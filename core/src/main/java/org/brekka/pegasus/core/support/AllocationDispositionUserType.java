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

package org.brekka.pegasus.core.support;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.brekka.commons.persistence.support.AbstractTypeUserType;
import org.brekka.pegasus.core.model.AllocationDisposition;
import org.brekka.pegasus.core.model.PegasusAllocationDisposition;

/**
 * Allocation EntityType User EntityType
 */
public class AllocationDispositionUserType extends AbstractTypeUserType<AllocationDisposition> {


    public AllocationDispositionUserType() {
        super(Arrays.asList(PegasusAllocationDisposition.class));
    }

    public AllocationDispositionUserType(final Map<String, AllocationDisposition> typesMap) {
        super(typesMap);
    }

    public <Type extends Enum<?> & AllocationDisposition> AllocationDispositionUserType(final List<Class<Type>> enumTypesList) {
        super(enumTypesList);
    }

    @Override
    public Class<AllocationDisposition> returnedClass() {
        return AllocationDisposition.class;
    }
}
