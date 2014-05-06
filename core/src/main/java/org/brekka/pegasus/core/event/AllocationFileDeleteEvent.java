/**
 * Copyright (c) 2014 Digital Shadows Ltd.
 */
package org.brekka.pegasus.core.event;

import org.brekka.pegasus.core.model.AllocationFile;
import org.springframework.context.ApplicationEvent;

/**
 * Event issued when an allocation file is deleted.
 *
 * @author Andrew Taylor (andy@digitalshadows.com)
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
