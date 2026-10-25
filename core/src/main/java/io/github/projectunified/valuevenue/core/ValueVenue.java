package io.github.projectunified.valuevenue.core;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Holds the values generated for each key.
 * <p>
 * Values are computed only when {@link #update(Object)} is called for a key; {@link #get(Object)} never
 * generates anything and returns an empty list until the key has been updated at least once.
 * <p>
 * Instances are safe for concurrent use; readers see either the values stored before or after an
 * {@link #update(Object)} call, never a partially generated list.
 *
 * @param <K> type of the key
 * @param <V> type of the generated value
 */
public class ValueVenue<K, V> {
    private final Function<K, List<V>> valueGenerator;
    private final Map<K, List<V>> valueListMap = new ConcurrentHashMap<>();

    /**
     * Creates a venue whose keys are updated through {@link #update(Object)}.
     *
     * @param valueGenerator generator invoked by {@link #update(Object)} to compute the values of a key
     */
    public ValueVenue(Function<K, List<V>> valueGenerator) {
        this.valueGenerator = valueGenerator;
    }

    /**
     * Returns the values stored for the key by the last {@link #update(Object)} call.
     *
     * @param key key to look up
     * @return read-only list of the stored values, empty if the key was never updated
     */
    public List<V> get(K key) {
        return Collections.unmodifiableList(valueListMap.getOrDefault(key, Collections.emptyList()));
    }

    /**
     * Generates the values of the key and stores them, replacing any previous values.
     *
     * @param key key to regenerate
     */
    public void update(K key) {
        List<V> values = valueGenerator.apply(key);
        valueListMap.put(key, values);
    }
}
