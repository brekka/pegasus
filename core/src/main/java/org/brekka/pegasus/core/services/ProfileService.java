/**
 * 
 */
package org.brekka.pegasus.core.services;

import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Profile;
import org.brekka.pegasus.core.model.Vault;

/**
 * For manipulating a user's profile.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface ProfileService {

    Profile createPlainProfile(Member member);
    
    Profile createEncryptedProfile(Member member, Vault vault);
    
    /**
     * Will retrieve the profile, extracting the model only if the profile is not encrypted.
     * @param member
     * @return
     */
    Profile retrieveProfile(Member member);

    /**
     * Attempt to release the specified profile with the given vault. If the xmlEntity was not encrypted
     * with this vault, just return false.
     * @param profile the profile to release
     * @param vault the vault to use to unlock the profile.
     * @return true if the profile was unlocked
     */
    boolean releaseProfile(Profile profile, Vault vault);
}
