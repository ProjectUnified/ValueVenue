package io.github.projectunified.valuevenue.common;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StringSplitSourceTest {
    private static java.util.List<String> values(StringSplitSource<String> source, String key) {
        return source.getSourceValueStream(key).collect(Collectors.toList());
    }

    @Test
    void splitsTheLinesByDefault() {
        StringSplitSource<String> source = new StringSplitSource<>(key -> "one\r\ntwo\nthree");

        assertEquals(Arrays.asList("one", "two", "three"), values(source, "key"));
    }

    @Test
    void splitsByTheGivenPattern() {
        StringSplitSource<String> source = new StringSplitSource<>(key -> "one,two,three", Pattern.compile(","));

        assertEquals(Arrays.asList("one", "two", "three"), values(source, "key"));
    }

    @Test
    void selectsTheStringPerKey() {
        StringSplitSource<String> source = new StringSplitSource<>(key -> key.length() > 2 ? "one\ntwo" : key);

        assertEquals(Collections.singletonList("ab"), values(source, "ab"));
        assertEquals(Arrays.asList("one", "two"), values(source, "abc"));
    }

    @Test
    void isEmptyWithoutAString() {
        StringSplitSource<String> source = new StringSplitSource<>(key -> null);

        assertEquals(Collections.emptyList(), values(source, "key"));
    }

    @Test
    void hasNoKeyIndependentValues() {
        StringSplitSource<String> source = new StringSplitSource<>(key -> "one");

        assertEquals(Collections.emptyList(), source.getSourceValueStream().collect(Collectors.toList()));
    }

    @Test
    void resolvesThePieceWithTheDefaultKey() {
        StringSplitSource<String> source = new StringSplitSource<>(key -> "one");

        assertEquals("one", source.replace(StringSplitSource.DEFAULT_KEY, "one"));
        assertNull(source.replace("other", "one"));
    }

    @Test
    void resolvesThePieceWithAGivenKey() {
        StringSplitSource<String> source = new StringSplitSource<>("line", key -> "one");

        assertEquals("one", source.replace("line", "one"));
        assertNull(source.replace(StringSplitSource.DEFAULT_KEY, "one"));
    }
}
