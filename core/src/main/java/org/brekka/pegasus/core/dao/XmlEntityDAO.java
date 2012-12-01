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
public interface XmlEntityDAO extends EntityDAO<UUID, XmlEntity<?>> {

    void create(XmlEntity<?> xmlEntity, InputStream inputStream, long length);

    /**
     * @param serial
     * @param version
     * @param b
     * @return
     */
    <T extends XmlObject> XmlEntity<T> retrieveBySerialVersion(UUID serial, int version, Class<T> xmlType);
}
