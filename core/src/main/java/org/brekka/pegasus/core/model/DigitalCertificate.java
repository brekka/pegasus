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

import java.util.Date;
import java.util.UUID;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.brekka.commons.persistence.model.SnapshotEntity;
import org.hibernate.annotations.Type;

/**
 * Captures the details of a digital certificate used to authenticate a user.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`DigitalCertificate`", uniqueConstraints=
@UniqueConstraint(columnNames={"`CertificateSubjectID`", "`Signature`"})
        )
public class DigitalCertificate extends SnapshotEntity<UUID> {
    /**
     * Serial UID
     */
    private static final long serialVersionUID = -4869048281179726403L;

    /**
     * Unique id
     */
    @Id
    @Type(type="pg-uuid")
    @Access(AccessType.PROPERTY)
    @Column(name="`ID`")
    private UUID id;

    /**
     * The signature of this certificate - surrogate key in combination with certificate authentication.
     */
    @Column(name="`Signature`")
    private byte[] signature;

    /**
     * The certificate authentication that this certificate belongs to.
     */
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="`CertificateSubjectID`", nullable=false)
    private CertificateSubject certificateSubject;

    /**
     * The moment this certificate is due to expire
     */
    @Column(name="`Expires`", nullable=false, updatable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date expires;

    /**
     * Is this certificate currently active? Setting to false essentially acts as certificate revocation.
     */
    @Column(name="`Active`")
    private Boolean active;

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.model.IdentifiableEntity#getId()
     */
    @Override
    public UUID getId() {
        return this.id;
    }

    /* (non-Javadoc)
     * @see org.brekka.commons.persistence.model.IdentifiableEntity#setId(java.io.Serializable)
     */
    @Override
    public void setId(final UUID id) {
        this.id = id;
    }

    /**
     * @return the expires
     */
    public Date getExpires() {
        return this.expires;
    }

    /**
     * @param expires the expires to set
     */
    public void setExpires(final Date expires) {
        this.expires = expires;
    }

    /**
     * @return the active
     */
    public Boolean getActive() {
        return this.active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(final Boolean active) {
        this.active = active;
    }

    /**
     * @return the signature
     */
    public byte[] getSignature() {
        return this.signature;
    }

    /**
     * @param signature the signature to set
     */
    public void setSignature(final byte[] signature) {
        this.signature = signature;
    }

    /**
     * @return the certificateSubject
     */
    public CertificateSubject getCertificateSubject() {
        return this.certificateSubject;
    }

    /**
     * @param certificateSubject the certificateSubject to set
     */
    public void setCertificateSubject(final CertificateSubject certificateSubject) {
        this.certificateSubject = certificateSubject;
    }
}
