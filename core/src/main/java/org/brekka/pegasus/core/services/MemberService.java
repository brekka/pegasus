/**
 *
 */
package org.brekka.pegasus.core.services;

import java.util.UUID;

import org.brekka.pegasus.core.model.ActorStatus;
import org.brekka.pegasus.core.model.AuthenticationToken;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.MemberContext;
import org.brekka.pegasus.core.model.Organization;
import org.brekka.pegasus.core.model.Person;
import org.brekka.pegasus.core.security.PegasusPrincipal;
import org.brekka.pegasus.core.security.PegasusPrincipalAware;
import org.brekka.xml.pegasus.v2.model.ProfileType;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author Andrew Taylor
 *
 */
public interface MemberService {

    /**
     * @return
     */
    boolean isNewMember();

    void setupPerson(ProfileType profileType, String vaultPassword, boolean encryptedProfile);

    Person createPerson(AuthenticationToken authenticationToken, ProfileType profileType, String vaultPassword, boolean encryptProfile);

    MemberContext getCurrent();

    MemberContext retrieveCurrent();

    void logout(PegasusPrincipal principal);

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
     * @param token
     * @param expected
     * @return
     */
    <T extends Member> T retrieveMember(AuthenticationToken token, Class<T> expected);

    /**
     * @param openID
     */
    Person createPerson(AuthenticationToken authenticationToken);

    /**
     * @param fromString
     * @param class1
     * @return
     */
    <T extends Member> T retrieveById(UUID memberId, Class<T> expected);

    /**
     * @param person
     */
    void resetMember(Member member);

    /**
     * @param actorId
     * @param status
     */
    void updateStatus(UUID actorId, ActorStatus status);

    MemberContext loginAndBind(PegasusPrincipalAware principalSource, String vaultPassword, Organization organization);

    void unbind();
}

