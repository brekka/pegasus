/**
 *
 */
package org.brekka.pegasus.core.services;

import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Profile;
import org.brekka.xml.pegasus.v2.model.ProfileType;

/**
 * For manipulating a user's profile.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface ProfileService {

    Profile createPlainProfile(Member member, ProfileType profileType);

    Profile createEncryptedProfile(Member member, ProfileType profileType, KeySafe<? extends Member> vault);

    /**
     * Will retrieve the profile, extracting the model only if the profile is not encrypted.
     * @param member
     * @return
     */
    Profile retrieveProfile(Member member);



    /**
     * Indicate that the profile for the current user has been updated
     * @param profile
     */
    void currentUserProfileUpdated();

    /**
     * @param profile
     */
    void delete(Profile profile);

    void deleteFor(Member member);

}
