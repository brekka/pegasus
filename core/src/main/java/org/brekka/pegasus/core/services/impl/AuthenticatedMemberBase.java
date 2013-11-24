/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * Retains the details for a logged-in member. It forms the basis of the {@link UserDetails} that is bound to the security
 * context for a given user.
 *
 * @author Andrew Taylor (andrew@brekka.org)
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

    /**
     * What access rights does this member have.
     */
    private final Set<GrantedAuthority> authorities;

    /**
     * Determines which actor is active. Normally this will be the same as 'person' but the user
     * can switch 'context' to for instance their associate entry.
     */
    private transient Actor activeActor;

    /**
     * The user-selected active profile
     */
    private transient Profile activeProfile;

    /**
     * The collection of Phalanx vault key references that are currently unlocked.
     */
    private final EntityUnlockKeyCache<AuthenticatedPrincipal> vaultKeyCache = new EntityUnlockKeyCache<>();

    /**
     * Collection of unlocked Phalanx private key references.
     */
    private final EntityUnlockKeyCache<PrivateKeyToken> privateKeyCache = new EntityUnlockKeyCache<>();

    /**
     * Essentially a cache of expensive to generate values (non-serializable).
     */
    private final AccessorContext context = new AccessorContextImpl();

    /**
     * Make sure the username is always available
     */
    private final String username;

    /**
     *
     */
    protected AuthenticatedMemberBase(final T member, final Set<GrantedAuthority> authorities) {
        this.member = member;
        setActiveActor(member);
        this.authorities = authorities;
        this.username = member.getAuthenticationToken().getUsername();
    }

    @SafeVarargs
    protected AuthenticatedMemberBase(final T member, final GrantedAuthority... authorities) {
        this(member, toSet(authorities));
    }


    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.AuthenticatedMember#getActiveActor()
     */
    @Override
    public Actor getActiveActor() {
        return this.activeActor;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.AuthenticatedMember#getProfile()
     */
    @Override
    public ProfileType getProfile() {
        if (this.activeProfile == null) {
            return null;
        }
        XmlEntity<ProfileDocument> xmlEntity = this.activeProfile.getXml();
        ProfileDocument bean = xmlEntity.getBean();
        if (bean == null) {
            return null;
        }
        return bean.getProfile();
    }

    /**
     * @param user
     */
    protected void addAuthority(final GrantedAuthority authority) {
        this.authorities.add(authority);
    }

    /**
     * @param memberSignup
     */
    protected void removeAuthority(final GrantedAuthority authority) {
        this.authorities.remove(authority);
    }

    void setMember(final T member) {
        this.member = member;
    }

    /**
     * @param activeProfile the activeProfile to set
     */
    void setActiveProfile(final Profile activeProfile) {
        this.activeProfile = activeProfile;
    }

    /**
     * @return the activeProfile
     */
    Profile getActiveProfile() {
        return this.activeProfile;
    }


    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.AuthenticatedMember#getVault(java.util.UUID)
     */
    AuthenticatedPrincipal getVaultKey(final Vault vault) {
        return this.vaultKeyCache.get(vault.getId());
    }

    /**
     * @param openVault
     */
    void retainVaultKey(final Vault vault) {
        this.vaultKeyCache.put(vault.getId(), vault.getAuthenticatedPrincipal());
    }

    PrivateKeyToken getPrivateKey(final KeyPair keyPair) {
        return this.privateKeyCache.get(keyPair.getId());
    }

    void retainPrivateKey(final KeyPair keyPair, final PrivateKeyToken privateKeyToken) {
        if (!keyPair.getId().equals(privateKeyToken.getKeyPair().getId())) {
            throw new PegasusException(PegasusErrorCode.PG104,
                    "Private key token does not belong to keyPair '%s'. It instead belongs to '%s'",
                    keyPair.getId(), privateKeyToken.getKeyPair().getId());
        }
        this.privateKeyCache.put(keyPair.getId(), privateKeyToken);
    }

    /**
     * @param activeActor the activeActor to set
     */
    protected void setActiveActor(final Actor activeActor) {
        this.activeActor = activeActor;
    }

    /**
     * @return
     */
    synchronized List<AuthenticatedPrincipal> clearVaults() {
        return this.vaultKeyCache.clear();
    }

    synchronized void clearVault(final Vault vault) {
        this.vaultKeyCache.remove(vault.getId());
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.Accessor#getContext()
     */
    @Override
    public AccessorContext getContext() {
        return this.context;
    }

    /**
     * @return the member
     */
    @Override
    public T getMember() {
        return this.member;
    }


    /* (non-Javadoc)
     * @see org.springframework.security.core.userdetails.UserDetails#getAuthorities()
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.AuthenticatedMember#hasAccess(org.springframework.security.core.GrantedAuthority)
     */
    @Override
    public boolean hasAccess(final GrantedAuthority grantedAuthority) {
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
        return this.username;
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


    static <T extends Member> AuthenticatedMemberBase<T> getCurrent(final MemberService memberService, final Class<T> expectedType) {
        AuthenticatedMember<T> current = memberService.getCurrent(expectedType);
        if (current == null) {
            return null;
        }
        if (current instanceof AuthenticatedMemberBase) {
            return (AuthenticatedMemberBase<T>) current;
        }
        throw new PegasusException(PegasusErrorCode.PG102, "'%s' is not a managed instance of '%s'",
                current.getClass().getName(), AuthenticatedMemberBase.class.getName());
    }

    public static boolean isAvailable(final MemberService memberService) {
        return memberService.getCurrent() != null;
    }


    /**
     * @param authorities2
     * @return
     */
    protected static <GA extends GrantedAuthority> Set<GA> toSet(final GA[] authoritiesArr) {
        Set<GA> authorities = new LinkedHashSet<>();
        for (GA pegasusAuthority : authoritiesArr) {
            authorities.add(pegasusAuthority);
        }
        return authorities;
    }
}
