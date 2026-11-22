package io.github.projectunified.valuevenue.core;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValueVenueTest {
    @Test
    void returnsNothingUntilTheKeyIsUpdated() {
        ValueVenue<String, String> venue = new ValueVenue<>(key -> Collections.singletonList(key + "-value"));

        assertEquals(Collections.emptyList(), venue.get("a"));
    }

    @Test
    void storesTheGeneratedValuesPerKey() {
        ValueVenue<String, String> venue = new ValueVenue<>(key -> Arrays.asList(key + "-1", key + "-2"));

        venue.update("a");

        assertEquals(Arrays.asList("a-1", "a-2"), venue.get("a"));
        assertEquals(Collections.emptyList(), venue.get("b"));
    }

    @Test
    void replacesThePreviousValuesOnUpdate() {
        AtomicInteger run = new AtomicInteger();
        ValueVenue<String, String> venue = new ValueVenue<>(key -> Collections.singletonList(key + "-" + run.incrementAndGet()));

        venue.update("a");
        String first = venue.get("a").get(0);
        venue.update("a");

        assertEquals(Collections.singletonList("a-2"), venue.get("a"));
        assertEquals("a-1", first);
    }

    @Test
    void returnsAReadOnlyDetachedList() {
        ValueVenue<String, String> venue = new ValueVenue<>(key -> Collections.singletonList(key));

        venue.update("a");

        assertThrows(UnsupportedOperationException.class, () -> venue.get("a").add("b"));
        assertNotSame(venue.get("a"), venue.get("a"));
    }
}
