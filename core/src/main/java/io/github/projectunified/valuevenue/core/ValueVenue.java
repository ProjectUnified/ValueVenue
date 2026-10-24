package io.github.projectunified.valuevenue.core;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ValueVenue<K, V> {
    private final Function<K, List<V>> valueGenerator;
    private final Map<K, List<V>> valueListMap = new ConcurrentHashMap<>();

    public ValueVenue(Function<K, List<V>> valueGenerator) {
        this.valueGenerator = valueGenerator;
    }

    public List<V> get(K key) {
        return Collections.unmodifiableList(valueListMap.getOrDefault(key, Collections.emptyList()));
    }

    public void update(K key) {
        List<V> values = valueGenerator.apply(key);
        valueListMap.put(key, values);
    }
}
