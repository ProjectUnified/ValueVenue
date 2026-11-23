package io.github.projectunified.valuevenue.common;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class KeyValueSourceTest {
    private final Map<String, Object> value = Collections.singletonMap("name", "Alex");

    @Test
    void resolvesTheDefaultKey() {
        KeyValueSource<String> stream = new KeyValueSource<>(Collections.singletonList(value));

        assertEquals("Alex", stream.replace(KeyValueSource.DEFAULT_KEY + "_name", value));
        assertEquals("", stream.replace(KeyValueSource.DEFAULT_KEY + "_missing", value));
        assertNull(stream.replace("name", value));
    }

    @Test
    void resolvesAGivenKey() {
        KeyValueSource<String> stream = new KeyValueSource<>("map", Collections.singletonList(value));

        assertEquals("Alex", stream.replace("map_name", value));
        assertNull(stream.replace(KeyValueSource.DEFAULT_KEY + "_name", value));
    }
}
