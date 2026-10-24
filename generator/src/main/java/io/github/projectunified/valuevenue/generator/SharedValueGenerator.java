package io.github.projectunified.valuevenue.generator;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class SharedValueGenerator<K, SV, FV> implements Function<K, List<FV>> {
    private final Map<SV, CachedValueEntry<FV>> cache = new ConcurrentHashMap<>();

    public abstract Stream<SV> getSourceValueStream();

    public abstract FV mapToFinalValue(SV sourceValue);

    public boolean isSourceValueActivated(SV value) {
        return true;
    }

    public boolean isValueActivated(FV value, K key) {
        return true;
    }

    @Override
    public List<FV> apply(K key) {
        return getSourceValueStream()
                .map(cache::get)
                .filter(Objects::nonNull)
                .filter(entry -> entry.activated.get())
                .filter(entry -> isValueActivated(entry.finalValue, key))
                .map(entry -> entry.finalValue)
                .collect(Collectors.toList());
    }

    public void update() {
        getSourceValueStream().forEach(sourceValue -> cache.compute(sourceValue, (sv, entry) -> {
            if (entry == null) {
                entry = new CachedValueEntry<>(mapToFinalValue(sv));
            }
            entry.activated.lazySet(isSourceValueActivated(sourceValue));
            return entry;
        }));
    }
}
