/*
 * Copyright 2014 the original author or authors.
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

package org.brekka.pegasus.core.event;

import org.brekka.pegasus.core.model.AllocationFile;
import org.springframework.context.ApplicationEvent;

/**
 * Event issued when an allocation file is deleted.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class AllocationFileDeleteEvent extends ApplicationEvent {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 2513713132499259187L;

    /**
     * @param source
     */
    public AllocationFileDeleteEvent(final AllocationFile source) {
        super(source);
    }

    public AllocationFile getAllocationFile() {
        return (AllocationFile) getSource();
    }
}
