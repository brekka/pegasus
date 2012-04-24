/**
 * 
 */
package org.brekka.pegasus.web.support;

import java.util.Collections;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;

/**
 * @author Andrew Taylor
 *
 */
public final class OpenIDUtils {

    /**
     * 
     */
    private OpenIDUtils() {
    }
    
    /**
     * @return
     */
    public static String identifyName() {
        List<OpenIDAttribute> attributes = getAttributes();
        String fullName = getSingleAttribute("fullname", attributes);
        if (fullName != null) {
            return fullName;
        }
        String firstName = getSingleAttribute("firstName", attributes);
        String lastName = getSingleAttribute("lastName", attributes);
        fullName = firstName;
        if (firstName == null) {
            fullName = lastName;
        } else if (lastName != null) {
            fullName = firstName + " " + lastName;
        }
        return fullName;
    }
    
    

    /**
     * @return
     */
    public static String identifyEmail() {
        return getSingleAttribute("email", getAttributes());
    }
    
    private static String getSingleAttribute(String name, List<OpenIDAttribute> attributes) {
        for (OpenIDAttribute openIDAttribute : attributes) {
            if (openIDAttribute.getName().equals(name)) {
                return openIDAttribute.getValues().get(0);
            }
        }
        return null;
    }
    

    private static List<OpenIDAttribute> getAttributes() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if (authentication instanceof OpenIDAuthenticationToken) {
            OpenIDAuthenticationToken token = (OpenIDAuthenticationToken) authentication;
            List<OpenIDAttribute> attributes = token.getAttributes();
            return attributes;
        }
        return Collections.emptyList();
    }
}
