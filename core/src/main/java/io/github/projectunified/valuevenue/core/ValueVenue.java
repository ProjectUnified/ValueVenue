package io.github.projectunified.valuevenue.core;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ValueVenue<K, V> {
    private final Function<K, List<V>> valueSupplier;
    private final Map<K, List<V>> valueListMap = new ConcurrentHashMap<>();

    public ValueVenue(Function<K, List<V>> valueSupplier) {
        this.valueSupplier = valueSupplier;
    }

    public List<V> get(K key) {
        return Collections.unmodifiableList(valueListMap.getOrDefault(key, Collections.emptyList()));
    }

    public void update(K key) {
        List<V> values = valueSupplier.apply(key);
        valueListMap.put(key, values);
    }
}
