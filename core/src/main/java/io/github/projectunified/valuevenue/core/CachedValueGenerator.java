package io.github.projectunified.valuevenue.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generates the final values for a key from source values, mapping and caching each source value once.
 * <p>
 * The cache is shared by every key and lives as long as the generator, so {@link #mapToFinalValue(Object)}
 * must be deterministic and must not depend on the key. Whether a cached value is exposed for a key is
 * decided per call by {@link #isValueActivated(Object, Object)}.
 * <p>
 * Instances are safe for concurrent {@link #apply(Object)} and {@link #update()} calls, but a cached
 * activation state is only recomputed by {@link #update()}; callers must invoke it when the activation
 * of source values changes.
 *
 * @param <K> type of the key used to select values
 * @param <SV> type of the source value
 * @param <FV> type of the final value
 */
public abstract class CachedValueGenerator<K, SV, FV> implements Function<K, List<FV>> {
    private final Map<SV, CachedValueEntry<FV>> cache = new ConcurrentHashMap<>();

    /**
     * All source values the generator knows without a key, so {@link #update()} can create their cache
     * entries upfront. Empty by default: a key-only generator needs no override, while its entries are
     * created lazily by {@link #apply(Object)}. Values that can only be reached through a key are returned
     * by {@link #getSourceValueStream(Object)} instead.
     *
     * @return stream of every key-independent source value
     */
    public Stream<SV> getSourceValueStream() {
        return Stream.empty();
    }

    /**
     * Source values to expose for the given key. Defaults to {@link #getSourceValueStream()}; override when
     * the source values depend on the key. Entries for key-only source values are created lazily by
     * {@link #apply(Object)}, not by {@link #update()}.
     *
     * @param key key whose source values are requested
     * @return stream of source values for the key
     */
    public Stream<SV> getSourceValueStream(K key) {
        return getSourceValueStream();
    }

    /**
     * Maps a source value to its final value. Called once per source value, then cached.
     *
     * @param sourceValue source value to map
     * @return final value of the source value
     */
    public abstract FV mapToFinalValue(SV sourceValue);

    /**
     * Whether a source value is currently active. The result is cached per source value; it is evaluated
     * when the entry is created and refreshed by {@link #update()}, so changes take effect after that call.
     *
     * @param value source value to check
     * @return true if the value may be exposed
     */
    public boolean isSourceValueActivated(SV value) {
        return true;
    }

    /**
     * Whether a final value is exposed for the given key. Not cached: evaluated on every
     * {@link #apply(Object)} call.
     *
     * @param value final value to check
     * @param key key the value would be exposed for
     * @return true if the value is visible to the key
     */
    public boolean isValueActivated(FV value, K key) {
        return true;
    }

    /**
     * Returns the active final values for the given key, in the order of its source value stream.
     *
     * @param key key to generate values for
     * @return list of the active final values, never null
     */
    @Override
    public List<FV> apply(K key) {
        return getSourceValueStream(key)
                // entries are created lazily, so apply does not depend on update() having run first
                .map(sourceValue -> cache.computeIfAbsent(sourceValue, sv -> {
                    CachedValueEntry<FV> entry = new CachedValueEntry<>(mapToFinalValue(sv));
                    entry.activated.lazySet(isSourceValueActivated(sv));
                    return entry;
                }))
                .filter(entry -> entry.activated.get())
                .filter(entry -> isValueActivated(entry.finalValue, key))
                .map(entry -> entry.finalValue)
                .collect(Collectors.toList());
    }

    /**
     * Creates the cache entries of all {@link #getSourceValueStream() common source values} and refreshes
     * the activation state of every cached entry, including entries created lazily for a key.
     * <p>
     * Entries are never evicted: a source value that disappears from the streams stays cached.
     */
    public void update() {
        getSourceValueStream().forEach(sourceValue -> cache.computeIfAbsent(sourceValue, sv -> new CachedValueEntry<>(mapToFinalValue(sv))));
        cache.forEach((sourceValue, entry) -> entry.activated.lazySet(isSourceValueActivated(sourceValue)));
    }

    /**
     * Cached mapping of one source value; the activation state mirrors the last
     * {@link #isSourceValueActivated(Object)} evaluation.
     */
    private static class CachedValueEntry<FV> {
        final FV finalValue;
        final AtomicBoolean activated = new AtomicBoolean();

        CachedValueEntry(FV finalValue) {
            this.finalValue = finalValue;
        }
    }
}
