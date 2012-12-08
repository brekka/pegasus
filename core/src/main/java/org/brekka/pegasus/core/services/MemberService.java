/**
 * 
 */
package org.brekka.pegasus.core.services;

import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.AuthenticationToken;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Person;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;

/**
 * @author Andrew Taylor
 *
 */
public interface MemberService {

    /**
     * @return
     */
    boolean isNewMember();

    void setupPerson(String name, String email, String vaultPassword, boolean encryptedProfile);
    
    Person createPerson(AuthenticationToken authenticationToken, String fullName, String email, String vaultPassword, boolean encryptProfile);
    
    <T extends Member> AuthenticatedMember<T> getCurrent(Class<T> expectedType);
    
    AuthenticatedMember<Member> getCurrent();

    /**
     * @param securityContext
     */
    void logout(SecurityContext securityContext);

    /**
     * @param organization
     */
    void activateOrganization(Organization organization);

    /**
     * 
     */
    void activateMember();

    /**
     * @param anonymousTransfer
     * @return
     */
    boolean hasAccess(GrantedAuthority anonymousTransfer);

    /**
     * @param digitalCertificate
     * @return
     */
    Person retrievePerson(AuthenticationToken token);

    /**
     * @param openID
     */
    Person createPerson(AuthenticationToken authenticationToken);

}

