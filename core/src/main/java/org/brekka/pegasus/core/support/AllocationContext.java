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

import java.util.HashMap;
import java.util.Map;

import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.model.Allocation;

/**
 * AllocationContext
 */
public class AllocationContext {

    private final Map<String, Allocation> allocations = new HashMap<>();

    public void register(final String key, final Allocation allocation) {
        allocations.put(key, allocation);
    }

    @SuppressWarnings("unchecked")
    public <T extends Allocation> T get(final String key, final Class<T> type) {
        Allocation allocation = allocations.get(key);
        if (allocation == null) {
            return null;
        }
        if (type.isAssignableFrom(allocation.getClass())) {
            return (T) allocation;
        }
        throw new PegasusException(PegasusErrorCode.PG601,
                "Keyed allocation '%s' type '%s' does not match the expected '%s'.",
                 key, allocation.getClass().getName(), type.getName());
    }

    public boolean has(final String makeKey) {
        return allocations.containsKey(makeKey);
    }
}
