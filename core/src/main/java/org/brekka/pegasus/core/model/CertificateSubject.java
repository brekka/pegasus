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

/**
 * Certificate based authentication based on subject DN. For more restrictive certificate handling, the actual digital certificate can be captured.  (see DigitalCertificate).
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@DiscriminatorValue("CertSubject")
public class CertificateSubject extends AuthenticationToken {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 8849307393573103449L;


    /**
     * The DistinguishedName digest
     */
    @Column(name="`DistinguishedName`", unique=true, length=255)
    private byte[] distinguishedNameDigest;
    
    /**
     * The un-digested distinguished name
     */
    @Transient
    private transient String distinguishedName;
    
    /**
     * Normally the CN part of the distinguished name
     */
    @Transient
    private transient String commonName;
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.AuthenticationToken#getUsername()
     */
    @Override
    public String getUsername() {
        return getCommonName();
    }

    /**
     * @return the distinguishedNameDigest
     */
    public byte[] getDistinguishedNameDigest() {
        return distinguishedNameDigest;
    }

    /**
     * @param distinguishedNameDigest the distinguishedNameDigest to set
     */
    public void setDistinguishedNameDigest(byte[] distinguishedNameDigest) {
        this.distinguishedNameDigest = distinguishedNameDigest;
    }

    /**
     * @return the distinguishedName
     */
    public String getDistinguishedName() {
        return distinguishedName;
    }

    /**
     * @param distinguishedName the distinguishedName to set
     */
    public void setDistinguishedName(String distinguishedName) {
        this.distinguishedName = distinguishedName;
    }

    /**
     * @param commonName the commonName to set
     */
    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }
    
    /**
     * @return the commonName
     */
    public String getCommonName() {
        return commonName;
    }
}
