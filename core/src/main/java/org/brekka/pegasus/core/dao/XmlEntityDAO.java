/**
 *
 */
package org.brekka.pegasus.core.dao;

import java.util.List;
import java.util.UUID;

import org.apache.xmlbeans.XmlObject;
import org.brekka.commons.persistence.dao.EntityDAO;
import org.brekka.pegasus.core.model.KeySafe;
import org.brekka.pegasus.core.model.XmlEntity;

public interface XmlEntityDAO extends EntityDAO<UUID, XmlEntity<?>> {

    /**
     * @param serial
     * @param version
     * @param b
     * @return
     */
    <T extends XmlObject> XmlEntity<T> retrieveBySerialVersion(byte[] serial, int version, Class<T> xmlType);

    /**
     * @param vault
     */
    List<XmlEntity<?>> retrieveByKeySafe(KeySafe<?> keySafe);

    /**
     * @param serial
     * @return
     */
    List<XmlEntity<?>> retrieveBySerial(byte[] serial);

    List<XmlEntity<?>> findExternalResourceBased(UUID afterId, int limit);
}
