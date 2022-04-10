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

package org.brekka.pegasus.core.support;

import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.xml.pegasus.v2.model.DetailsType;

/**
 * Build a details object for an allocation
 */
public class AllocationDetailsBuilder<T extends DetailsType> {

    protected final T details;

    /**
     * @param details
     */
    @SuppressWarnings("unchecked")
    public AllocationDetailsBuilder(final Class<T> detailsClass) {
        SchemaType schemaType = XmlBeans.getContextTypeLoader().typeForClassname(detailsClass.getName());
        XmlObject xmlObject = XmlBeans.getContextTypeLoader().newInstance(schemaType, null);
        if (!detailsClass.isAssignableFrom(xmlObject.getClass())) {
            throw new PegasusException(PegasusErrorCode.PG591, "Failed to create instance of '%s'", detailsClass.getName());
        }
        this.details = (T) xmlObject;
    }

    public AllocationDetailsBuilder(final T details) {
        this.details = details;
    }

    public AllocationDetailsBuilder<T> setAgreementText(final String agreementText) {
        if (StringUtils.isNotBlank(agreementText)) {
            details.setAgreement(agreementText);
        }
        return this;
    }

    public AllocationDetailsBuilder<T> setDescription(final String description, final Object... args) {
        if (StringUtils.isNotBlank(description)) {
            details.setDescription(String.format(description, args));
        }
        return this;
    }

    public AllocationDetailsBuilder<T> setComment(final String comment) {
        if (StringUtils.isNotBlank(comment)) {
            details.setComment(comment);
        }
        return this;
    }

    public AllocationDetailsBuilder<T> setReference(final String reference) {
        if (StringUtils.isNotBlank(reference)) {
            details.setReference(reference);
        }
        return this;
    }

    public AllocationDetailsBuilder<T> setSubject(final String subject) {
        if (StringUtils.isNotBlank(subject)) {
            details.setSubject(subject);
        }
        return this;
    }

    public T toDetailsType() {
        return details;
    }
}
