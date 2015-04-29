/**
 *
 */
package org.brekka.pegasus.core.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Caches the phalanx key that is used by the entity.
 *
 * @author Andrew Taylor (andrew@brekka.org)
 */
public class EntityUnlockKeyCache<T> implements Serializable {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = 5415576494466862394L;

    private transient Map<UUID, T> entities;

    public synchronized void put(final UUID key, final T value) {
        cache().put(key, value);
    }

    public synchronized T get(final UUID uuid) {
        return cache().get(uuid);
    }

    public synchronized void remove(final UUID uuid) {
        cache().remove(uuid);
    }

    public synchronized List<T> clear() {
        List<T> values = new ArrayList<>(cache().values());
        entities.clear();
        entities = null;
        return values;
    }

    private Map<UUID, T> cache() {
        Map<UUID, T> map = this.entities;
        if (map == null) {
            this.entities = new HashMap<>();
        }
        return this.entities;
    }

    /**
     * @param privateKeyCache
     */
    public void putAll(final EntityUnlockKeyCache<T> privateKeyCache) {
        cache().putAll(privateKeyCache.entities);
    }
}
