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
import org.brekka.xml.pegasus.v2.model.EMailType;
import org.brekka.xml.pegasus.v2.model.ProfileType;

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
    
    @Property
    private boolean encryptProfile = true;

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
        ProfileType profileType = ProfileType.Factory.newInstance();
        profileType.setFullName(name);
        EMailType profileEmail = profileType.addNewEMail();
        profileEmail.setAddress(email);
        memberService.setupPerson(profileType, vaultPassword, encryptProfile);
        return MemberIndex.class;
    }
}
