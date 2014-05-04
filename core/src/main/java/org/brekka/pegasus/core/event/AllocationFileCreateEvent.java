/**
 * Copyright (c) 2014 Digital Shadows Ltd.
 */
package org.brekka.pegasus.core.event;

import org.brekka.pegasus.core.model.AllocationFile;
import org.springframework.context.ApplicationEvent;

/**
 * Event issued when a new allocation file is created.
 *
 * @author Andrew Taylor (andy@digitalshadows.com)
 */
public class AllocationFileCreateEvent extends ApplicationEvent {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 2513713132499259187L;

    /**
     * @param source
     */
    public AllocationFileCreateEvent(final AllocationFile source) {
        super(source);
    }

    public AllocationFile getAllocationFile() {
        return (AllocationFile) getSource();
    }
}
