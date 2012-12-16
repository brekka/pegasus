/**
 * 
 */
package org.brekka.pegasus.core.services.impl;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.pegasus.core.model.AccessorContext;
import org.brekka.pegasus.core.model.Actor;
import org.brekka.pegasus.core.model.AuthenticatedMember;
import org.brekka.pegasus.core.model.Member;
import org.brekka.pegasus.core.model.Profile;
import org.brekka.pegasus.core.model.Vault;
import org.brekka.pegasus.core.model.XmlEntity;
import org.brekka.pegasus.core.services.MemberService;
import org.brekka.pegasus.core.utils.EntityUnlockKeyCache;
import org.brekka.phalanx.api.model.AuthenticatedPrincipal;
import org.brekka.phalanx.api.model.KeyPair;
import org.brekka.phalanx.api.model.PrivateKeyToken;
import org.brekka.xml.pegasus.v2.model.ProfileDocument;
import org.brekka.xml.pegasus.v2.model.ProfileType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public abstract class AuthenticatedMemberBase<T extends Member> implements AuthenticatedMember<T>, UserDetails {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -5476667151062439957L;
    
    /**
     * Will always be the person instance that corresponds to the login.
     */
    private T member;

    
    private final Set<GrantedAuthority> authorities;
    
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
    
    private final AccessorContext context = new AccessorContextImpl();
    
    /**
     * 
     */
    protected AuthenticatedMemberBase(T member, Set<GrantedAuthority> authorities) {
        this.member = member;
        setActiveActor(member);
        this.authorities = authorities;
    }
    
    @SafeVarargs
    protected AuthenticatedMemberBase(T member, GrantedAuthority... authorities) {
        this(member, toSet(authorities));
    }
    

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
    
    /**
     * @param user
     */
    protected void addAuthority(GrantedAuthority authority) {
        authorities.add(authority);
    }

    /**
     * @param memberSignup
     */
    protected void removeAuthority(GrantedAuthority authority) {
        authorities.remove(authority);
    }
    
    void setMember(T member) {
        this.member = member;
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
    protected void setActiveVault(Vault activeVault) {
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
    AuthenticatedPrincipal getVaultKey(Vault vault) {
        return vaultKeyCache.get(vault.getId());
    }

    /**
     * @param openVault
     */
    void retainVaultKey(Vault vault) {
        vaultKeyCache.put(vault.getId(), vault.getAuthenticatedPrincipal());
    }
    
    PrivateKeyToken getPrivateKey(KeyPair keyPair) {
        return privateKeyCache.get(keyPair.getId());
    }
    
    void retainPrivateKey(KeyPair keyPair, PrivateKeyToken privateKeyToken) {
        if (!keyPair.getId().equals(privateKeyToken.getKeyPair().getId())) {
            throw new PegasusException(PegasusErrorCode.PG104, 
                    "Private key token does not belong to keyPair '%s'. It instead belongs to '%s'",
                    keyPair.getId(), privateKeyToken.getKeyPair().getId());
        }
        privateKeyCache.put(keyPair.getId(), privateKeyToken);
    }
    
    /**
     * @param activeActor the activeActor to set
     */
    protected void setActiveActor(Actor activeActor) {
        this.activeActor = activeActor;
    }

    /**
     * @return
     */
    synchronized List<AuthenticatedPrincipal> clearVaults() {
        this.activeVault = null;
        return vaultKeyCache.clear();
    }
    
    synchronized void clearVault(Vault vault) {
        vaultKeyCache.remove(vault.getId());
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.Accessor#getContext()
     */
    @Override
    public AccessorContext getContext() {
        return context;
    }
    
    /**
     * @return the member
     */
    @Override
    public T getMember() {
        return member;
    }
    

    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#getAuthorities()
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.AuthenticatedMember#hasAccess(org.springframework.security.core.GrantedAuthority)
     */
    @Override
    public boolean hasAccess(GrantedAuthority grantedAuthority) {
        return getAuthorities().contains(grantedAuthority);
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#getPassword()
     */
    @Override
    public String getPassword() {
        return "notused";
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#getUsername()
     */
    @Override
    public String getUsername() {
        return member.getAuthenticationToken().getUsername();
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#isAccountNonExpired()
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#isAccountNonLocked()
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#isCredentialsNonExpired()
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    
    static <T extends Member> AuthenticatedMemberBase<T> getCurrent(MemberService memberService, Class<T> expectedType) {
        AuthenticatedMember<T> current = memberService.getCurrent(expectedType);
        if (current instanceof AuthenticatedMemberBase) {
            return (AuthenticatedMemberBase<T>) current;
        }
        throw new PegasusException(PegasusErrorCode.PG102, "'%s' is not a managed instance of '%s'",
                current.getClass().getName(), AuthenticatedMemberBase.class.getName());
    }
    
    public static boolean isAvailable(MemberService memberService) {
        return memberService.getCurrent() != null;
    }


    /**
     * @param authorities2
     * @return
     */
    protected static <GA extends GrantedAuthority> Set<GA> toSet(GA[] authoritiesArr) {
        Set<GA> authorities = new LinkedHashSet<>();
        for (GA pegasusAuthority : authoritiesArr) {
            authorities.add(pegasusAuthority);
        }
        return authorities;
    }
}
