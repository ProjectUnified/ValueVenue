package io.github.projectunified.valuevenue.common;

import io.github.projectunified.valuevenue.core.CachedValueGenerator;
import io.github.projectunified.valuevenue.core.ValueSource;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EnumValueSourceTest {
    private enum Sample {
        FIRST, SECOND
    }

    private final Map<Sample, Integer> conversions = new HashMap<>();

    private CachedValueGenerator<String, Sample, String> createGenerator(ValueSource<String, Sample> source) {
        return CachedValueGenerator.create(source, (sourceValue, variableReplacer) -> {
            conversions.merge(sourceValue, 1, Integer::sum);
            return variableReplacer.apply(EnumValueSource.DEFAULT_KEY) + ":" + sourceValue.name().toLowerCase();
        });
    }

    private EnumValueSource<String, Sample> createSource() {
        return new EnumValueSource<>(Sample.class);
    }

    @Test
    void exposesTheConstantsAsSourceValues() {
        assertEquals(Arrays.asList("FIRST:first", "SECOND:second"), createGenerator(createSource()).apply("key"));
        assertEquals(Integer.valueOf(1), conversions.get(Sample.FIRST));
        assertEquals(Integer.valueOf(1), conversions.get(Sample.SECOND));
    }

    @Test
    void createsTheEntriesOfEveryConstantOnUpdate() {
        CachedValueGenerator<String, Sample, String> generator = createGenerator(createSource());

        generator.update();

        assertEquals(2, generator.getCachedValues().size());
        assertEquals("FIRST:first", generator.getCachedValues().get(Sample.FIRST));
    }

    @Test
    void hidesTheInactiveConstants() {
        ValueSource<String, Sample> source = new EnumValueSource<String, Sample>(Sample.class) {
            @Override
            public boolean isSourceValueActivated(Sample value) {
                return value != Sample.SECOND;
            }
        };

        assertEquals(Collections.singletonList("FIRST:first"), createGenerator(source).apply("key"));
    }

    @Test
    void resolvesTheConstantWithTheDefaultKey() {
        EnumValueSource<String, Sample> source = createSource();

        assertEquals(Sample.FIRST, source.replace(EnumValueSource.DEFAULT_KEY, Sample.FIRST));
        assertNull(source.replace("other", Sample.FIRST));
    }

    @Test
    void resolvesTheConstantWithAGivenKey() {
        EnumValueSource<String, Sample> source = new EnumValueSource<>("sample", Sample.class);

        assertEquals(Sample.FIRST, source.replace("sample", Sample.FIRST));
        assertNull(source.replace(EnumValueSource.DEFAULT_KEY, Sample.FIRST));
    }
}
