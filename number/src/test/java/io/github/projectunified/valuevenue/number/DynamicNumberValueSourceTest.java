package io.github.projectunified.valuevenue.number;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DynamicNumberValueSourceTest {
    private final DynamicNumberValueSource<String, Integer> source = new DynamicNumberValueSource<String, Integer>(NumberType.INT) {
        @Override
        protected Number getStart(String key) {
            return 0;
        }

        @Override
        protected Number getEnd(String key) {
            return key.length();
        }

        @Override
        protected Number getStep(String key) {
            return key.length() > 2 ? 2 : 1;
        }
    };

    @Test
    void buildsTheRangeOfEachKey() {
        assertEquals(Arrays.asList("0", "1", "2"), valuesOf("ab"));
        assertEquals(Arrays.asList("0", "2", "4"), valuesOf("abcd"));
        // an empty key gives an end of 0, which is the start itself
        assertEquals(Collections.singletonList("0"), valuesOf(""));
    }

    @Test
    void resolvesTheNumberAsPlainText() {
        assertEquals("2", source.replace(DynamicNumberValueSource.DEFAULT_KEY, 2));
    }

    @Test
    void hasNoKeyIndependentValues() {
        assertEquals(Collections.emptyList(), source.getSourceValueStream().collect(Collectors.toList()));
    }

    private java.util.List<String> valuesOf(String key) {
        return source.getSourceValueStream(key).map(String::valueOf).collect(Collectors.toList());
    }
}
