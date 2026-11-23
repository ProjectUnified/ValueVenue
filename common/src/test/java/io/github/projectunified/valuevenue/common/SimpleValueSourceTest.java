package io.github.projectunified.valuevenue.common;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SimpleValueSourceTest {
    private final Function<String, Object> formatter = value -> "formatted-" + value;

    @Test
    void resolvesTheFormattedValueWithTheDefaultKey() {
        SimpleValueSource<String, String> source = new SimpleValueSource<>(Arrays.asList("one", "two"), formatter);

        assertEquals("formatted-one", source.replace(SimpleValueSource.DEFAULT_KEY, "one"));
        assertNull(source.replace("other", "one"));
    }

    @Test
    void exposesTheValuesAsTheyAre() {
        SimpleValueSource<String, String> source = new SimpleValueSource<>(Arrays.asList("one", "two"), formatter);

        assertEquals(Arrays.asList("one", "two"), source.getSourceValueStream().collect(Collectors.toList()));
    }

    @Test
    void resolvesTheFormattedValueWithAGivenKey() {
        SimpleValueSource<String, String> source = new SimpleValueSource<>("value", Collections.singletonList("one"), formatter);

        assertEquals("formatted-one", source.replace("value", "one"));
        assertNull(source.replace(SimpleValueSource.DEFAULT_KEY, "one"));
    }

    @Test
    void resolvesTheValueWithoutAFormatterWhenItIsTheIdentity() {
        SimpleValueSource<String, Integer> source = new SimpleValueSource<>(Collections.singletonList(5), value -> value);

        assertEquals(5, source.replace(SimpleValueSource.DEFAULT_KEY, 5));
    }
}
