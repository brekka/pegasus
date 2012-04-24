/**
 * 
 */
package org.brekka.pegasus.web.pages;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.services.MemberService;

/**
 * @author Andrew Taylor
 *
 */
public class Member {
    
    @Inject
    private MemberService memberService;

    Object onActivate() {
        Object retVal = Boolean.TRUE;
        if (memberService.isNewMember()) {
            retVal = Setup.class;
        }
        return retVal;
    }
    
}
