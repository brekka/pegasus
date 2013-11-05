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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * A simple attachment implementation
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public final class SimpleFileAttachment implements Attachment, Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 2904444235305015102L;
    
    private final File file;
    private final String name;
    private final String contentType;
    
    /* (non-Javadoc)
     * @see org.springframework.core.io.InputStreamSource#getInputStream()
     */
    /**
     * @param inputStream
     * @param name
     * @param contentType
     * @param length
     */
    public SimpleFileAttachment(File file, String name, String contentType) {
        this.file = file;
        this.name = name;
        this.contentType = contentType;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.Attachment#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.Attachment#getContentType()
     */
    @Override
    public String getContentType() {
        return contentType;
    }

    /* (non-Javadoc)
     * @see org.brekka.pegasus.core.model.Attachment#getLength()
     */
    @Override
    public long getLength() {
        return file.length();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("name", name)
            .append("contentType", contentType)
            .append("file", file)
            .toString();
    }
}
