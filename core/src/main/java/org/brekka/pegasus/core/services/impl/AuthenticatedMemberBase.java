/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.List;
import java.util.UUID;

import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.Profile;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.model.XmlEntity;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.utils.EntityUnlockKeyCache;
import org.brekka.phalanx.api.model.AuthenticatedPrincipal;
import org.brekka.phalanx.api.model.PrivateKeyToken;
import org.brekka.xml.pegasus.v1.model.ProfileDocument;
import org.brekka.xml.pegasus.v1.model.ProfileType;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
abstract class AuthenticatedMemberBase implements AuthenticatedMember {

    /**
     * Determines which actor is active. Normally this will be the same as 'person' but the user
     * can switch 'context' to for instance their associate entry.
     */
    private transient Actor activeActor;
    
    /**
     * The active vault
     */
    private transient Vault activeVault;
    
    /**
     * The user-selected active profile
     */
    private transient Profile activeProfile;
    
    /**
     * The collection of Phalanx vault key references that are currently unlocked.
     */
    private EntityUnlockKeyCache<AuthenticatedPrincipal> vaultKeyCache = new EntityUnlockKeyCache<>();
    
    /**
     * Collection of unlocked Phalanx private key references.
     */
    private EntityUnlockKeyCache<PrivateKeyToken> privateKeyCache = new EntityUnlockKeyCache<>();
    

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.AuthenticatedMember#getActiveActor()
     */
    @Override
    public Actor getActiveActor() {
        return activeActor;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.AuthenticatedMember#getProfile()
     */
    @Override
    public ProfileType getProfile() {
        if (activeProfile == null) {
            return null;
        }
        XmlEntity<ProfileDocument> xmlEntity = activeProfile.getXml();
        ProfileDocument bean = xmlEntity.getBean();
        if (bean == null) {
            return null;
        }
        return bean.getProfile();
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.AuthenticatedMember#getActiveVault()
     */
    @Override
    public Vault getActiveVault() {
        return activeVault;
    }

    /**
     * @param activeVault the activeVault to set
     */
    void setActiveVault(Vault activeVault) {
        this.activeVault = activeVault;
    }
    
    /**
     * @param activeProfile the activeProfile to set
     */
    void setActiveProfile(Profile activeProfile) {
        this.activeProfile = activeProfile;
    }
    
    /**
     * @return the activeProfile
     */
    Profile getActiveProfile() {
        return activeProfile;
    }
    

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.AuthenticatedMember#getVault(java.util.UUID)
     */
    AuthenticatedPrincipal getVaultKey(UUID vaultId) {
        return vaultKeyCache.get(vaultId);
    }

    /**
     * @param openVault
     */
    void retainVaultKey(UUID vaultId, AuthenticatedPrincipal authenticatedPrincipal) {
        vaultKeyCache.put(vaultId, authenticatedPrincipal);
    }
    
    PrivateKeyToken getPrivateKey(UUID entityId) {
        return privateKeyCache.get(entityId);
    }
    
    void retainPrivateKey(UUID entityId, PrivateKeyToken privateKeyToken) {
        privateKeyCache.put(entityId, privateKeyToken);
    }
    
    /**
     * @param activeActor the activeActor to set
     */
    void setActiveActor(Actor activeActor) {
        this.activeActor = activeActor;
    }

    /**
     * @return
     */
    synchronized List<AuthenticatedPrincipal> clearVaults() {
        this.activeVault = null;
        return vaultKeyCache.clear();
    }
    
    static AuthenticatedMemberBase getCurrent(MemberService memberService) {
        AuthenticatedMember current = memberService.getCurrent();
        if (current instanceof AuthenticatedMemberBase) {
            return (AuthenticatedMemberBase) current;
        }
        throw new PegasusException(PegasusErrorCode.PG102, "'%s' is not a managed instance of '%s'",
                current.getClass().getName(), AuthenticatedMemberBase.class.getName());
    }
}
