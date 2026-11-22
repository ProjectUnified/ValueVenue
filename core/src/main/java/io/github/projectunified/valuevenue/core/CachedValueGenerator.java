package io.github.projectunified.valuevenue.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
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
 * <p>
 * Which source values exist, and whether they are active, is decided by {@link #getSourceValueStream()} and
 * {@link #isSourceValueActivated(Object)}; use {@link #create(ValueSource, BiFunction)} to take both from a
 * {@link ValueSource} instead of overriding them.
 *
 * @param <K> type of the key used to select values
 * @param <SV> type of the source value
 * @param <FV> type of the final value
 */
public abstract class CachedValueGenerator<K, SV, FV> implements Function<K, List<FV>> {
    private final Map<SV, CachedValueEntry<FV>> cache = new ConcurrentHashMap<>();

    /**
     * Creates a generator from a source and a conversion, exposing every final value to every key.
     *
     * @param source    the source values
     * @param converter the conversion of a source value and its variable replacer to its final value
     * @param <K>       type of the key
     * @param <SV>      type of the source value
     * @param <FV>      type of the final value
     * @return the generator
     */
    public static <K, SV, FV> CachedValueGenerator<K, SV, FV> create(ValueSource<K, SV> source, BiFunction<SV, Function<String, Object>, FV> converter) {
        return create(source, converter, (value, key) -> true);
    }

    /**
     * Creates a generator from a source and a conversion.
     *
     * @param source      the source values
     * @param converter   the conversion of a source value and its variable replacer to its final value
     * @param valueFilter whether a final value is exposed to the key, evaluated on every {@link #apply(Object)} call
     * @param <K>         type of the key
     * @param <SV>        type of the source value
     * @param <FV>        type of the final value
     * @return the generator
     */
    public static <K, SV, FV> CachedValueGenerator<K, SV, FV> create(ValueSource<K, SV> source, BiFunction<SV, Function<String, Object>, FV> converter, BiPredicate<FV, K> valueFilter) {
        return new CachedValueGenerator<K, SV, FV>() {
            @Override
            public Stream<SV> getSourceValueStream() {
                return source.getSourceValueStream();
            }

            @Override
            public Stream<SV> getSourceValueStream(K key) {
                return source.getSourceValueStream(key);
            }

            @Override
            public boolean isSourceValueActivated(SV value) {
                return source.isSourceValueActivated(value);
            }

            @Override
            public FV mapToFinalValue(SV sourceValue) {
                return converter.apply(sourceValue, input -> source.replace(input, sourceValue));
            }

            @Override
            public boolean isValueActivated(FV value, K key) {
                return valueFilter.test(value, key);
            }
        };
    }

    /**
     * All source values the generator knows without a key, so {@link #update()} can create their cache entries
     * upfront. Empty by default: a key-only generator needs no override, while its entries are created lazily by
     * {@link #apply(Object)}.
     *
     * @return stream of every key-independent source value
     */
    public Stream<SV> getSourceValueStream() {
        return Stream.empty();
    }

    /**
     * Source values to expose for the given key. Defaults to {@link #getSourceValueStream()}.
     *
     * @param key key whose source values are requested
     * @return stream of source values for the key
     */
    public Stream<SV> getSourceValueStream(K key) {
        return getSourceValueStream();
    }

    /**
     * Whether a source value is currently active. The result is cached per source value; it is evaluated when the
     * entry is created and refreshed by {@link #update()}.
     *
     * @param value source value to check
     * @return true if the value may be exposed
     */
    public boolean isSourceValueActivated(SV value) {
        return true;
    }

    /**
     * Maps a source value to its final value. Called once per source value, then cached.
     *
     * @param sourceValue source value to map
     * @return final value of the source value
     */
    public abstract FV mapToFinalValue(SV sourceValue);

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
     * Returns a snapshot of the cache as source value to final value, including entries whose source value
     * is not active and entries created by a key.
     * <p>
     * The map is read-only and detached: later {@link #apply(Object)} or {@link #update()} calls do not
     * change it.
     *
     * @return read-only copy of the cached mapping
     */
    public Map<SV, FV> getCachedValues() {
        Map<SV, FV> values = new HashMap<>(cache.size());
        cache.forEach((sourceValue, entry) -> values.put(sourceValue, entry.finalValue));
        return Collections.unmodifiableMap(values);
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
