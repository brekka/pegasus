/**
 *
 */
package org.brekka.pegasus.core.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

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

    private final Supplier<Map<UUID, T>> mapSupplier;

    private transient Map<UUID, T> entities;

    public EntityUnlockKeyCache(final Supplier<Map<UUID, T>> mapSupplier) {
        this.mapSupplier = mapSupplier;
    }

    public synchronized void put(final UUID key, final T value) {
        cache().put(key, value);
    }

    public synchronized T get(final UUID uuid) {
        return cache().get(uuid);
    }

    public synchronized void remove(final UUID uuid) {
        if (entities == null) {
            return;
        }
        cache().remove(uuid);
    }

    public synchronized List<T> clear() {
        if (entities != null) {
            List<T> values = new ArrayList<>(entities.values());
            entities.clear();
            entities = null;
            return values;
        }
        return Collections.emptyList();
    }

    private Map<UUID, T> cache() {
        if (entities == null) {
            entities = mapSupplier.get();
        }
        return entities;
    }

    public void putAll(final EntityUnlockKeyCache<T> privateKeyCache) {
        Map<UUID, T> otherMap = privateKeyCache.entities;
        if (otherMap != null) {
            cache().putAll(otherMap);
        }
    }
}
