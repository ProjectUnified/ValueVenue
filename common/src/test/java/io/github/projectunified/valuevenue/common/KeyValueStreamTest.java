package io.github.projectunified.valuevenue.common;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class KeyValueStreamTest {
    private final Map<String, Object> value = Collections.singletonMap("name", "Alex");

    @Test
    void resolvesTheDefaultKey() {
        KeyValueStream stream = new KeyValueStream(Collections.singletonList(value));

        assertEquals("Alex", stream.replace(KeyValueStream.DEFAULT_KEY + "_name", value));
        assertEquals("", stream.replace(KeyValueStream.DEFAULT_KEY + "_missing", value));
        assertNull(stream.replace("name", value));
    }

    @Test
    void resolvesAGivenKey() {
        KeyValueStream stream = new KeyValueStream("map", Collections.singletonList(value));

        assertEquals("Alex", stream.replace("map_name", value));
        assertNull(stream.replace(KeyValueStream.DEFAULT_KEY + "_name", value));
    }
}
