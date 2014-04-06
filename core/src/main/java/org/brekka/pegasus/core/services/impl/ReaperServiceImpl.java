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

package org.brekka.pegasus.core.services.impl;

import java.util.Collections;
import java.util.List;

import org.brekka.pegasus.core.dao.AllocationDAO;
import org.brekka.pegasus.core.dao.AllocationFileDAO;
import org.brekka.pegasus.core.model.Allocation;
import org.brekka.pegasus.core.model.AllocationFile;
import org.brekka.pegasus.core.services.AllocationService;
import org.brekka.pegasus.core.services.ReaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

/**
 * The reaper uses background threads to delete allocations once they have expired.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
// Should be declared explicitly
// @Service
public class ReaperServiceImpl implements ReaperService {

    @Autowired
    private AllocationDAO allocationDAO;

    @Autowired
    private AllocationFileDAO allocationFileDAO;

    @Autowired
    private AllocationService allocationService;

    // TODO configured
    private final int maxAllocationCount = 20;
    // TODO configured
    private final int maxAllocationFileCount = 20;


    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.ReaperService#clearAllocationFiles()
     */
    @Override
    @Scheduled(fixedDelay=5000) // Gap of five seconds between each invocation
    @Transactional()
    public void clearAllocationFiles() {
        List<AllocationFile> allocationFileList = Collections.emptyList();
        do {
            allocationFileList = this.allocationFileDAO.retrieveOldestExpired(this.maxAllocationFileCount);
            for (AllocationFile allocationFile : allocationFileList) {
                this.allocationService.clearAllocationFile(allocationFile);
            }
            // Keep looping until there are no more entries to expire
        } while (!allocationFileList.isEmpty());
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.ReaperService#clearAllocations()
     */
    @Override
    @Scheduled(fixedDelay=10000) // Gap of ten seconds between each invocation
    @Transactional()
    public void clearAllocations() {
        List<Allocation> allocationList = Collections.emptyList();
        do {
            allocationList = this.allocationDAO.retrieveOldestExpired(this.maxAllocationCount);
            for (Allocation allocation : allocationList) {
                this.allocationService.clearAllocation(allocation);
            }
            // Keep looping until there are no more entries to expire
        } while (!allocationList.isEmpty());
    }
}
