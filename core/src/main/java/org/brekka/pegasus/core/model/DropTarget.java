/**
 * 
 */
package org.brekka.pegasus.core.model;

import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

/**
 * Enable a member to give out a URL that enables others to send them files.
 * 
 * @author Andrew Taylor
 */
public class DropTarget {

    /**
     * The URL fragment that identifies the member.
     */
    @OneToOne
    private Slug slug;
    
    /**
     * The member receiving the file
     */
    @ManyToOne
    private Member member;
    
    /**
     * The friendly name given to this target for user reference
     */
    private String name;
}
