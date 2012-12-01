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

package org.brekka.pegasus.core.services;

import java.security.cert.X509Certificate;
import java.util.List;

import org.brekka.pegasus.core.model.AuthenticationToken;
import org.brekka.pegasus.core.model.CertificateSubject;
import org.brekka.pegasus.core.model.DigitalCertificate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;

/**
 * Provides operations around {@link CertificateSubject} based {@link AuthenticationToken}s.
 * 
 * @author Andrew Taylor (andrew@brekka.org)
 */
public interface CertificateAuthenticationService {

    /**
     * Retrieve or create a {@link CertificateSubject} for the specified subject distinct name. The full DN should be
     * specified so that this services' rules can be applied. If the DN is not acceptable then a BadCredentialsException
     * will be thrown.
     * 
     * @param subjectDN
     *            the subject DN to authenticate.
     * @return the digital certificate instance assigned to this subject DN
     * @throws BadCredentialsException
     *             if the certificate is rejected.
     * @throws DisabledException
     *             if the certificate is recognised but has been disabled (via {@link DigitalCertificate#getActive()}).
     */
    DigitalCertificate authenticate(X509Certificate certificate) throws BadCredentialsException, DisabledException;

    /**
     * Set whether the digital certificate is enable or disabled
     * 
     * @param certificate
     *            the digital certificate
     * @param enabled
     *            whether this subjectDN is enabled or disabled.
     */
    void setEnabled(DigitalCertificate certificate, boolean enabled);

    /**
     * Retrieve the list of digital certificates for the specified {@link CertificateSubject}. Intended to allow an
     * administrator to revoke a given certificate (as an alternative to proper certificate revocation which could take
     * time).
     * 
     * @param certificateAuthentication
     *            the certificate subject to retrieve actual certificates for.
     * @return the list of certificates.
     */
    List<DigitalCertificate> retrieveSubjectCertificates(CertificateSubject certificateSubject);
}
