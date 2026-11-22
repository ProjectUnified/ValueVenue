package io.github.projectunified.valuevenue.core;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValueSourceTest {
    @Test
    void hasNoSourceValueByDefault() {
        ValueSource<String, String> source = new ValueSource<String, String>() {
            @Override
            public Object replace(String input, String value) {
                return null;
            }
        };

        assertEquals(Collections.emptyList(), source.getSourceValueStream().collect(Collectors.toList()));
        assertEquals(Collections.emptyList(), source.getSourceValueStream("key").collect(Collectors.toList()));
        assertTrue(source.isSourceValueActivated("value"));
    }

    @Test
    void exposesTheActiveValuesOfAKey() {
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
                return null;
            }
        };

        assertEquals(Collections.singletonList("one"), source.apply("key"));
    }

    @Test
    void canBeTheGeneratorOfAVenue() {
        ValueSource<String, String> source = new ValueSource<String, String>() {
            @Override
            public Stream<String> getSourceValueStream() {
                return Stream.of("one", "two");
            }

            @Override
            public Object replace(String input, String value) {
                return null;
            }
        };
        ValueVenue<String, String> venue = new ValueVenue<>(source);

        assertEquals(Collections.emptyList(), venue.get("key"));
        venue.update("key");
        assertEquals(Arrays.asList("one", "two"), venue.get("key"));
    }

    @Test
    void exposesTheKeyIndependentValuesForEveryKey() {
        ValueSource<String, String> source = new ValueSource<String, String>() {
            @Override
            public Stream<String> getSourceValueStream() {
                return Stream.of("one", "two");
            }

            @Override
            public Object replace(String input, String value) {
                return null;
            }
        };

        assertEquals(Arrays.asList("one", "two"), source.getSourceValueStream("key").collect(Collectors.toList()));
    }

    @Test
    void canSelectTheValuesPerKey() {
        ValueSource<String, String> source = new ValueSource<String, String>() {
            @Override
            public Stream<String> getSourceValueStream(String key) {
                return Stream.of(key);
            }

            @Override
            public Object replace(String input, String value) {
                return null;
            }
        };

        assertEquals(Collections.singletonList("a"), source.getSourceValueStream("a").collect(Collectors.toList()));
        assertEquals(Collections.singletonList("b"), source.getSourceValueStream("b").collect(Collectors.toList()));
    }
}
