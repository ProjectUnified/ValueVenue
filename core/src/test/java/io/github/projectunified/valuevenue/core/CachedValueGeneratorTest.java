package io.github.projectunified.valuevenue.core;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CachedValueGeneratorTest {
    private final Map<String, Integer> conversions = new HashMap<>();
    private final Set<String> activated = new HashSet<>(Arrays.asList("one", "two"));
    private final AtomicInteger valueChecks = new AtomicInteger();

    private CachedValueGenerator<String, String, String> createGenerator() {
        return new CachedValueGenerator<String, String, String>() {
            @Override
            public Stream<String> getSourceValueStream() {
                return Stream.of("one", "two");
            }

            @Override
            public String mapToFinalValue(String sourceValue) {
                conversions.merge(sourceValue, 1, Integer::sum);
                return "final-" + sourceValue;
            }

            @Override
            public boolean isSourceValueActivated(String value) {
                return activated.contains(value);
            }

            @Override
            public boolean isValueActivated(String value, String key) {
                valueChecks.incrementAndGet();
                return !key.equals("guest");
            }
        };
    }

    @Test
    void convertsEachSourceValueOnceAndSharesItAcrossKeys() {
        CachedValueGenerator<String, String, String> generator = createGenerator();

        assertEquals(Arrays.asList("final-one", "final-two"), generator.apply("a"));
        assertEquals(Arrays.asList("final-one", "final-two"), generator.apply("b"));
        assertEquals(Integer.valueOf(1), conversions.get("one"));
        assertEquals(Integer.valueOf(1), conversions.get("two"));
    }

    @Test
    void hidesTheInactiveSourceValues() {
        CachedValueGenerator<String, String, String> generator = createGenerator();
        activated.remove("two");

        assertEquals(Collections.singletonList("final-one"), generator.apply("a"));
    }

    @Test
    void refreshesTheSourceActivationOnUpdate() {
        CachedValueGenerator<String, String, String> generator = createGenerator();
        assertEquals(2, generator.apply("a").size());

        activated.remove("two");
        generator.update();
        assertEquals(Collections.singletonList("final-one"), generator.apply("a"));

        activated.add("two");
        generator.update();
        assertEquals(Arrays.asList("final-one", "final-two"), generator.apply("a"));
        // the cached results are reused, not generated again
        assertEquals(Integer.valueOf(1), conversions.get("one"));
        assertEquals(Integer.valueOf(1), conversions.get("two"));
    }

    @Test
    void checksTheValueActivationOnEveryCall() {
        CachedValueGenerator<String, String, String> generator = createGenerator();

        assertEquals(2, generator.apply("a").size());
        assertEquals(Collections.emptyList(), generator.apply("guest"));
        assertEquals(4, valueChecks.get());
    }

    @Test
    void usesTheSourceStreamOfTheKey() {
        CachedValueGenerator<String, String, String> generator = new CachedValueGenerator<String, String, String>() {
            @Override
            public Stream<String> getSourceValueStream(String key) {
                return Stream.of(key);
            }

            @Override
            public String mapToFinalValue(String sourceValue) {
                return "final-" + sourceValue;
            }
        };

        assertEquals(Collections.singletonList("final-a"), generator.apply("a"));
        assertEquals(Collections.singletonList("final-b"), generator.apply("b"));
    }

    @Test
    void keepsTheValuesThatDisappearedFromTheStream() {
        Set<String> source = new HashSet<>(Arrays.asList("one", "two"));
        CachedValueGenerator<String, String, String> generator = new CachedValueGenerator<String, String, String>() {
            @Override
            public Stream<String> getSourceValueStream() {
                return source.stream();
            }

            @Override
            public String mapToFinalValue(String sourceValue) {
                return "final-" + sourceValue;
            }
        };
        generator.apply("a");

        source.remove("two");
        generator.update();

        assertEquals(Collections.singletonList("final-one"), generator.apply("a"));
        assertEquals("final-two", generator.getCachedValues().get("two"));
    }

    @Test
    void createsAGeneratorFromASource() {
        ValueSource<String, String> source = new ValueSource<String, String>() {
            @Override
            public Stream<String> getSourceValueStream() {
                return Stream.of("one", "two");
            }

            @Override
            public boolean isSourceValueActivated(String value) {
                return !value.equals("two");
            }

            @Override
            public Object replace(String input, String value) {
                return input.equals("value") ? value : null;
            }
        };

        CachedValueGenerator<String, String, String> generator = CachedValueGenerator.create(source,
                (sourceValue, variableReplacer) -> variableReplacer.apply("value") + "-final",
                (value, key) -> !key.equals("guest"));

        assertEquals(Collections.singletonList("one-final"), generator.apply("a"));
        assertEquals(Collections.emptyList(), generator.apply("guest"));
        assertEquals(Collections.singletonList("one-final"), generator.apply("b"));
    }

    @Test
    void createsAGeneratorWithoutAValueFilter() {
        ValueSource<String, String> source = new ValueSource<String, String>() {
            @Override
            public Stream<String> getSourceValueStream() {
                return Stream.of("one");
            }

            @Override
            public Object replace(String input, String value) {
                return null;
            }
        };

        CachedValueGenerator<String, String, String> generator = CachedValueGenerator.create(source, (sourceValue, variableReplacer) -> sourceValue);

        assertEquals(Collections.singletonList("one"), generator.apply("guest"));
    }

    @Test
    void returnsAReadOnlyCache() {
        CachedValueGenerator<String, String, String> generator = createGenerator();
        generator.apply("a");

        Map<String, String> cached = generator.getCachedValues();

        assertEquals("final-one", cached.get("one"));
        assertThrows(UnsupportedOperationException.class, () -> cached.put("three", "final-three"));
    }
}
