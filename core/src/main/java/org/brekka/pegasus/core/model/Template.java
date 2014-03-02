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

package org.brekka.pegasus.core.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.brekka.commons.persistence.model.SnapshotEntity;
import org.brekka.pegasus.core.PegasusConstants;
import org.brekka.pegasus.core.PegasusErrorCode;
import org.brekka.pegasus.core.PegasusException;
import org.brekka.xml.pegasus.v2.model.TemplateDocument;
import org.brekka.xml.pegasus.v2.model.TemplateType;
import org.hibernate.annotations.Type;

/**
 * A text template that can be used for e-mail, SMS or other purpose. Can be referenced via a unique string slug or token. The actual
 * content is stored in an {@link XmlEntity} to give the option of encryption.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
@Entity
@Table(name="`Template`", schema=PegasusConstants.SCHEMA)
public class Template extends SnapshotEntity<UUID> implements XmlEntityAware<TemplateDocument>  {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -3495924634770257184L;

    /**
     * Unique id
     */
    @Id
    @Type(type="pg-uuid")
    @Column(name="`ID`")
    private UUID id;

    /**
     * Allows this template to be optionally located via token (if set).
     */
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="`TokenID`")
    private Token token;

    /**
     * Identify this template via a slug (optional).
     */
    @Column(name="`Slug`", unique=true, length=120)
    private String slug;

    /**
     * Optionally set a plaintext label.
     */
    @Column(name="`Label`", length=200)
    private String label;

    /**
     * Template engine to use
     */
    @Column(name="`Engine`", nullable=false, length=12)
    @Enumerated(EnumType.STRING)
    private TemplateEngine engine;

    /**
     * The template details.
     */
    @OneToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="`XmlEntityID`")
    private XmlEntity<TemplateDocument> xml;

    /**
     * Was this template imported and has it not been changed by an operator since it was imported?
     * Used to detect whether a template can be updated automatically.
     */
    @Column(name="`Imported`")
    private Boolean imported;

    /**
     * @return the id
     */
    @Override
    public UUID getId() {
        return this.id;
    }

    /**
     * @param id the id to set
     */
    @Override
    public void setId(final UUID id) {
        this.id = id;
    }

    /**
     * @return the token
     */
    public Token getToken() {
        return this.token;
    }

    /**
     * @param token the token to set
     */
    public void setToken(final Token token) {
        this.token = token;
    }

    /**
     * @return the slug
     */
    public String getSlug() {
        return this.slug;
    }

    /**
     * @param slug the slug to set
     */
    public void setSlug(final String key) {
        this.slug = key;
    }

    /**
     * @return the xml
     */
    @Override
    public XmlEntity<TemplateDocument> getXml() {
        return this.xml;
    }

    /**
     * @param xml the xml to set
     */
    @Override
    public void setXml(final XmlEntity<TemplateDocument> xml) {
        this.xml = xml;
    }

    /**
     * @return the engine
     */
    public TemplateEngine getEngine() {
        return this.engine;
    }

    /**
     * @param engine the engine to set
     */
    public void setEngine(final TemplateEngine engine) {
        this.engine = engine;
    }


    /**
     * @return the label
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(final String label) {
        this.label = label;
    }

    public TemplateType details() {
        return details(TemplateType.class);
    }


    public Boolean getImported() {
        return this.imported;
    }

    public void setImported(final Boolean imported) {
        this.imported = imported;
    }

    /**
     * Retrieve the details contained within the XML. Named without 'get' so as not to be handled as property.
     * @param expectedType
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends TemplateType> T details(final Class<T> expectedType) {
        TemplateDocument doc = this.xml.getBean();
        if (doc == null) {
            throw new PegasusException(PegasusErrorCode.PG876, "Template[%s] XML entity is locked", getId());
        }
        TemplateType details = doc.getTemplate();
        if (details == null) {
            // perfectly acceptable to be null.
            return null;
        }
        if (expectedType.isAssignableFrom(details.getClass())) {
            return (T) details;
        }
        throw new PegasusException(PegasusErrorCode.PG812, "Expected template details of type '%s', actual '%s'",
                expectedType.getName(), details.getClass().getName());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("id", this.id)
        .append("slug", this.slug)
        .append("token", this.token)
        .append("engine", this.engine)
        .append("label", this.label)
        .append("xml", this.xml)
        .toString();
    }
}
