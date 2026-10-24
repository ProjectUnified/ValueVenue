package io.github.projectunified.valuevenue.core;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class CachedValueSupplier<K, SV, FV> implements Function<K, List<FV>> {
    private final Map<SV, ValueEntry> cache = new ConcurrentHashMap<>();

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
                entry = new ValueEntry(sv, mapToFinalValue(sv));
            }
            entry.activated.lazySet(isSourceValueActivated(sourceValue));
            return entry;
        }));
    }

    private class ValueEntry {
        final SV sourceValue;
        final FV finalValue;
        final AtomicBoolean activated = new AtomicBoolean();

        private ValueEntry(SV sourceValue, FV finalValue) {
            this.sourceValue = sourceValue;
            this.finalValue = finalValue;
        }
    }
}
