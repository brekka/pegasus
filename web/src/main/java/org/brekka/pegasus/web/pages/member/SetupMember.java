/**
 * 
 */
package org.brekka.pegasus.web.pages.member;

import org.apache.commons.lang.StringUtils;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.web.support.OpenIDUtils;

/**
 * @author Andrew Taylor
 *
 */
public class SetupMember {
    
    @InjectComponent
    private Form setup;
    
    @Inject
    private MemberService memberService;
    
    @Property
    private String name;
    
    @Property
    private String vaultPassword;
    
    @Property
    private String vaultPasswordRepeat;

    Object onActivate() {
        name = OpenIDUtils.identifyName();
        Object retVal = Boolean.TRUE;
        if (!memberService.isNewMember()) {
            retVal = MemberIndex.class;
        }
        return retVal;
    }

    void onValidateForm() {
        if (!StringUtils.equals(vaultPassword, vaultPasswordRepeat)) {
            setup.recordError("The passwords do not match");
        }
    }
    
    Object onSuccess() {
        String email = OpenIDUtils.identifyEmail();
        memberService.setupMember(name, email, vaultPassword);
        return MemberIndex.class;
    }
}
