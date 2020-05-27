/**
 *
 */
package org.brekka.pegasus.core.utils;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

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

    private transient Cache<UUID, T> entities;

    public synchronized void put(final UUID key, final T value) {
        cache().put(key, value);
    }

    public synchronized T get(final UUID uuid) {
        return cache().getIfPresent(uuid);
    }

    public synchronized void remove(final UUID uuid) {
        cache().invalidate(uuid);
    }

    public synchronized List<T> clear() {
        List<T> values = new ArrayList<>(cache().asMap().values());
        entities.invalidateAll();
        entities = null;
        return values;
    }

    private Cache<UUID, T> cache() {
        Cache<UUID, T> cache = this.entities;
        if (cache == null) {
            cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(10))
                .maximumSize(100)
                .build();
        }
        return this.entities;
    }

    /**
     * @param privateKeyCache
     */
    public void putAll(final EntityUnlockKeyCache<T> privateKeyCache) {
        cache().putAll(privateKeyCache.entities.asMap());
    }
}
