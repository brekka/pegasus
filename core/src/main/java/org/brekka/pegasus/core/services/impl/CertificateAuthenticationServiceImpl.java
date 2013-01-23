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

package org.brekka.pegasus.core.services.impl;

import java.nio.charset.Charset;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.BooleanUtils;
import org.brekka.pegasus.core.dao.CertificateSubjectDAO;
import org.brekka.pegasus.core.dao.DigitalCertificateDAO;
import org.brekka.pegasus.core.model.CertificateSubject;
import org.brekka.pegasus.core.model.DigitalCertificate;
import org.brekka.pegasus.core.services.CertificateAuthenticationService;
import org.brekka.phoenix.api.CryptoProfile;
import org.brekka.phoenix.api.DerivedKey;
import org.brekka.phoenix.api.services.DerivedKeyCryptoService;
import org.brekka.stillingar.api.annotations.ConfigurationListener;
import org.brekka.stillingar.api.annotations.Configured;
import org.brekka.xml.pegasus.v2.config.CertificateAuthenticationServiceDocument;
import org.brekka.xml.pegasus.v2.config.SystemDerivedKeySpecType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the digital certificate service.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Service
@Transactional
@Configured
public class CertificateAuthenticationServiceImpl implements CertificateAuthenticationService {

    @Autowired
    private CertificateSubjectDAO certificateSubjectDAO;
    
    @Autowired
    private DigitalCertificateDAO digitalCertificateDAO;
    
    @Autowired
    private DerivedKeyCryptoService derivedKeyCryptoService;
    
    /**
     * Service configuration, set by {@link #configure(org.brekka.xml.pegasus.v2.config.CertificateAuthenticationServiceDocument.CertificateAuthenticationService)}.
     */
    private CertificateAuthenticationServiceDocument.CertificateAuthenticationService config;
    
    /**
     * The list of certificate subject DN patterns to allow access.
     */
    private List<Pattern> allowedSubjectDistinguishedNamePatterns;
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.CertificateAuthenticationService#authenticate(java.security.cert.X509Certificate)
     */
    @Override
    @Transactional()
    public DigitalCertificate authenticate(X509Certificate certificate) throws BadCredentialsException, DisabledException {
        byte[] signature = certificate.getSignature();
        String subjectDN = certificate.getSubjectDN().getName();
        String commonName = null;
        
        Matcher matcher = matchAllowedSubjectDN(subjectDN, allowedSubjectDistinguishedNamePatterns);
        if (matcher.groupCount() > 0) {
            commonName = matcher.group(1);
        }
        
        byte[] subjectDNBytes = subjectDN.getBytes(Charset.forName("UTF-8"));
        SystemDerivedKeySpecType spec = config.getSubjectDerivedKeySpec();
        
        DerivedKey derivedKey = derivedKeyCryptoService.apply(subjectDNBytes, spec.getSalt(), 
                                       null, CryptoProfile.Static.of(spec.getCryptoProfile()));
        byte[] distinguishedNameDigest = derivedKey.getDerivedKey();
        CertificateSubject certificateSubject = certificateSubjectDAO.retrieveByDistinguishedNameDigest(distinguishedNameDigest);
        if (certificateSubject == null) {
            // Create it
            certificateSubject = new CertificateSubject();
            certificateSubject.setDistinguishedNameDigest(distinguishedNameDigest);
            certificateSubjectDAO.create(certificateSubject);
        }
        
        DigitalCertificate digitalCertificate = digitalCertificateDAO.retrieveBySubjectAndSignature(certificateSubject, signature);
        if (digitalCertificate == null) {
            digitalCertificate = new DigitalCertificate();
            digitalCertificate.setActive(Boolean.TRUE);
            digitalCertificate.setCertificateSubject(certificateSubject);
            digitalCertificate.setCreated(certificate.getNotBefore());
            digitalCertificate.setExpires(certificate.getNotAfter());
            digitalCertificate.setSignature(signature);
            digitalCertificateDAO.create(digitalCertificate);
        }
        
        // Perform some checks
        if (BooleanUtils.isNotTrue(digitalCertificate.getActive())) {
            throw new DisabledException(String.format(
                    "The certficate with id '%s' has been disabled", digitalCertificate.getId()));
        }
        if (digitalCertificate.getExpires().before(new Date())) {
            throw new CredentialsExpiredException(String.format(
                    "The certficate with id '%s' expired %tF", digitalCertificate.getId(), digitalCertificate.getExpires()));
        }
        
        // Both of these are transient
        certificateSubject.setCommonName(commonName);
        certificateSubject.setDistinguishedName(subjectDN);
        return digitalCertificate;
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.CertificateAuthenticationService#retrieveSubjectCertificates(org.brekka.pegasus.core.model.CertificateSubject)
     */
    @Override
    @Transactional(readOnly=true)
    public List<DigitalCertificate> retrieveSubjectCertificates(CertificateSubject certificateSubject) {
        return digitalCertificateDAO.retrieveForSubject(certificateSubject);
    }
    
    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.services.CertificateAuthenticationService#setEnabled(org.brekka.pegasus.core.model.DigitalCertificate, boolean)
     */
    @Override
    @Transactional(isolation=Isolation.REPEATABLE_READ)
    public void setEnabled(DigitalCertificate certificate, boolean enabled) {
        DigitalCertificate managed = digitalCertificateDAO.retrieveById(certificate.getId());
        managed.setActive(Boolean.valueOf(enabled));
        digitalCertificateDAO.update(managed);
    }
    
    @ConfigurationListener
    public void configure(@Configured
                            CertificateAuthenticationServiceDocument.CertificateAuthenticationService config) {
        this.config = config;
        List<Pattern> allowedSubjectDistinguishedNamePatterns = new ArrayList<>();
        List<String> subjectDistinguishedNamePatternList = config.getSubjectDistinguishedNamePatternList();
        for (String patternString : subjectDistinguishedNamePatternList) {
            allowedSubjectDistinguishedNamePatterns.add(Pattern.compile(patternString));
        }
        this.allowedSubjectDistinguishedNamePatterns = allowedSubjectDistinguishedNamePatterns;
    }
    

    /**
     * @param subjectDN
     * @param allowedSubjectDistinguishedNamePatterns2
     */
    protected Matcher matchAllowedSubjectDN(String subjectDN, List<Pattern> allowedSubjectDistinguishedNamePatterns) {
        Matcher positiveMatcher = null;
        for (Pattern pattern : allowedSubjectDistinguishedNamePatterns) {
            Matcher matcher = pattern.matcher(subjectDN);
            if (matcher.matches()) {
                positiveMatcher = matcher;
                break;
            }
        }
        if (positiveMatcher == null) {
            throw new BadCredentialsException(String.format(
                    "The subject DN '%s' is not allowed to access this system", subjectDN));
        }
        return positiveMatcher;
    }
}
