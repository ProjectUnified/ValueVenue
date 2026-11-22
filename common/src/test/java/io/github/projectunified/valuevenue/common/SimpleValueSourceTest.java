package io.github.projectunified.valuevenue.common;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SimpleValueSourceTest {
    @Test
    void resolvesTheDefaultKey() {
        SimpleValueSource stream = new SimpleValueSource(Arrays.asList("one", "two"));

        assertEquals("one", stream.replace(SimpleValueSource.DEFAULT_KEY, "one"));
        assertNull(stream.replace("other", "one"));
        assertEquals(Arrays.asList("one", "two"), stream.getSourceValueStream().collect(java.util.stream.Collectors.toList()));
    }

    @Test
    void resolvesAGivenKey() {
        SimpleValueSource stream = new SimpleValueSource("value", Collections.singletonList("one"));

        assertEquals("one", stream.replace("value", "one"));
        assertNull(stream.replace(SimpleValueSource.DEFAULT_KEY, "one"));
    }
}
