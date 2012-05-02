/**
 * 
 */
package org.brekka.pegasus.core.dao;

import java.io.InputStream;
import java.util.UUID;

import org.apache.xmlbeans.XmlObject;
import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.XmlEntity;

/**
 * @author Andrew Taylor (andrew@brekka.org)
 *
 */
public interface XmlEntityDAO<T extends XmlObject> extends EntityDAO<UUID, XmlEntity<T>> {

    void update(XmlEntity<T> xmlEntity, InputStream inputStream);
}
