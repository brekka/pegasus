/*
 * Copyright 2012 the original author or authors.
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

package org.brekka.pegasus.core.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.brekka.phoenix.api.CryptoProfile;
import org.brekka.phoenix.api.DerivedKey;

/**
 * Username and Password based authentication token
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@DiscriminatorValue("UserPass")
public class UsernamePassword extends AuthenticationToken implements DerivedKey {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -5091518838128894183L;

    /**
     * The username digest
     */
    @Column(name="`Username`", unique=true, length=255)
    private byte[] usernameDigest;
    
    /**
     * The password digest
     */
    @Column(name="`Password`")
    private byte[] password;
    
    /**
     * The salt to combine with the username/password.
     */
    @Column(name="`Salt`")
    private byte[] salt;
    
    /**
     * The number of iterations of the algorthm to perform.
     */
    @Column(name="`Iterations`")
    private int iterations;
    
    /**
     * Identifies which checksum algorithm to use.
     */
    @Column(name="`Profile`")
    private int profile;
    
    /**
     * The un-digested username
     */
    @Transient
    private transient String username;
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.AuthenticationToken#getUsername()
     */
    @Override
    public String getUsername() {
        return username;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.AuthenticationToken#getUsername()
     */
    public byte[] getUsernameDigest() {
        return usernameDigest;
    }

    /**
     * @return the password
     */
    public byte[] getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(byte[] password) {
        this.password = password;
    }

    /**
     * @return the salt
     */
    public byte[] getSalt() {
        return salt;
    }

    /**
     * @param salt the salt to set
     */
    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    /**
     * @return the iterations
     */
    public Integer getIterations() {
        return iterations;
    }

    /**
     * @param iterations the iterations to set
     */
    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    /**
     * @return the profile
     */
    public int getProfile() {
        return profile;
    }

    /**
     * @param profile the profile to set
     */
    public void setProfile(int profile) {
        this.profile = profile;
    }

    /**
     * @param usernameDigest the usernameDigest to set
     */
    public void setUsernameDigest(byte[] usernameDigest) {
        this.usernameDigest = usernameDigest;
    }
    
    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.phoenix.api.DerivedKey#getDerivedKey()
     */
    @Override
    public byte[] getDerivedKey() {
        return getPassword();
    }
    
    /* (non-Javadoc)
     * @see org.brekka.phoenix.api.CryptoSpec#getCryptoProfile()
     */
    @Override
    public CryptoProfile getCryptoProfile() {
        return CryptoProfile.Static.of(getProfile());
    }
}
