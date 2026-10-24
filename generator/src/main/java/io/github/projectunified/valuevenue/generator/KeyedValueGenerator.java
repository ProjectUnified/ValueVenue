package io.github.projectunified.valuevenue.generator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class KeyedValueGenerator<K, SV, FV> implements Function<K, List<FV>> {
    private final Map<SV, CachedValueEntry<FV>> cache = new ConcurrentHashMap<>();

    public abstract Stream<SV> getSourceValueStream(K key);

    public abstract FV mapToFinalValue(SV sourceValue);

    public boolean isSourceValueActivated(SV value) {
        return true;
    }

    public boolean isValueActivated(FV value, K key) {
        return true;
    }

    @Override
    public List<FV> apply(K k) {
        return getSourceValueStream(k)
                .map(sv -> cache.computeIfAbsent(sv, v -> new CachedValueEntry<>(mapToFinalValue(v))))
                .filter(entry -> entry.activated.get())
                .filter(entry -> isValueActivated(entry.finalValue, k))
                .map(entry -> entry.finalValue)
                .collect(Collectors.toList());
    }

    public void update() {
        cache.forEach((sv, entry) -> entry.activated.lazySet(isSourceValueActivated(sv)));
    }
}
