package io.github.projectunified.valuevenue.number;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NumberValueSourceTest {
    private static <N extends Number> java.util.List<N> values(NumberValueSource<String, N> source) {
        return source.getSourceValueStream().collect(Collectors.toList());
    }

    @Test
    void stepsUpToTheExcludedEnd() {
        NumberValueSource<String, Integer> source = new NumberValueSource<>(NumberType.INT, 0, 5, 1, false);

        assertEquals(Arrays.asList(0, 1, 2, 3, 4), values(source));
    }

    @Test
    void includesTheEndByDefault() {
        assertEquals(Arrays.asList(0, 1, 2, 3, 4, 5), values(new NumberValueSource<>(NumberType.INT, 0, 5, 1)));
    }

    @Test
    void stepsByTheGivenStep() {
        assertEquals(Arrays.asList(0, 2, 4), values(new NumberValueSource<>(NumberType.INT, 0, 5, 2)));
    }

    @Test
    void stepsDownWhenTheStartIsHigher() {
        NumberValueSource<String, Integer> source = new NumberValueSource<>(NumberType.INT, 4, 0, 1);

        assertEquals(Arrays.asList(4, 3, 2, 1, 0), values(source));
        assertEquals("4", source.replace(NumberValueSource.DEFAULT_KEY, 4));
    }

    @Test
    void stepsDownUpToTheExcludedEnd() {
        assertEquals(Arrays.asList(5, 4, 3, 2, 1), values(new NumberValueSource<>(NumberType.INT, 5, 0, 1, false)));
    }

    @Test
    void producesTheNumberTypes() {
        assertEquals(Arrays.asList(0L, 1L, 2L), values(new NumberValueSource<>(NumberType.LONG, 0L, 2L, 1L)));
        assertEquals(Arrays.asList(0.5f, 1.0f, 1.5f), values(new NumberValueSource<>(NumberType.FLOAT, 0.5f, 1.5f, 0.5f)));
        assertEquals(Arrays.asList((byte) 0, (byte) 1), values(new NumberValueSource<>(NumberType.BYTE, 0, 1, 1)));
    }

    @Test
    void keepsTheFractionalStepExact() {
        NumberValueSource<String, Double> source = new NumberValueSource<>(NumberType.DOUBLE, 0.1, 0.3, 0.1);

        assertEquals(Arrays.asList(0.1, 0.2, 0.3), values(source));
    }

    @Test
    void resolvesTheNumberAsPlainText() {
        NumberValueSource<String, Double> source = new NumberValueSource<>(NumberType.DOUBLE, 10000000, 10000001, 1);

        assertEquals("10000000", source.replace(NumberValueSource.DEFAULT_KEY, 1.0E7));
        assertEquals("0.000000001", source.replace(NumberValueSource.DEFAULT_KEY, 1.0E-9));
        assertNull(source.replace("other", 1.0E7));
    }

    @Test
    void keepsTheIntegralValuesPlain() {
        NumberValueSource<String, Long> source = new NumberValueSource<>(NumberType.LONG, Long.MAX_VALUE - 1, Long.MAX_VALUE, 1);

        assertEquals(Arrays.asList(Long.MAX_VALUE - 1, Long.MAX_VALUE), values(source));
        assertEquals("9223372036854775807", source.replace(NumberValueSource.DEFAULT_KEY, Long.MAX_VALUE));
        assertEquals("-9223372036854775808", NumberType.LONG.format(Long.MIN_VALUE));
        assertEquals("2147483647", NumberType.INT.format(Integer.MAX_VALUE));
        assertEquals("-128", NumberType.BYTE.format(Byte.MIN_VALUE));
    }

    @Test
    void keepsTheDecimalValuesPlain() {
        assertEquals("10000000", NumberType.DOUBLE.format(1.0E7));
        assertEquals("100000000000000000000", NumberType.DOUBLE.format(1.0E20));
        assertEquals("0.000000001", NumberType.DOUBLE.format(1.0E-9));
        assertEquals("0.5", NumberType.FLOAT.format(0.5f));
    }

    @Test
    void resolvesTheNumberWithAGivenKey() {
        NumberValueSource<String, Integer> source = new NumberValueSource<>(NumberType.INT, "count", 0, 2, 1, true, Direction.AUTO);

        assertEquals("2", source.replace("count", 2));
        assertNull(source.replace(NumberValueSource.DEFAULT_KEY, 2));
    }

    @Test
    void detectsTheDirectionFromTheBounds() {
        assertEquals(Arrays.asList(0, 1, 2), values(new NumberValueSource<>(NumberType.INT, 0, 2, 1, true, Direction.AUTO)));
        assertEquals(Arrays.asList(2, 1, 0), values(new NumberValueSource<>(NumberType.INT, 2, 0, 1, true, Direction.AUTO)));
    }

    @Test
    void ascendsOnlyWhenTheEndIsHigher() {
        assertEquals(Arrays.asList(0, 1, 2), values(new NumberValueSource<>(NumberType.INT, 0, 2, 1, true, Direction.ASCENDING)));
        assertEquals(Collections.emptyList(), values(new NumberValueSource<>(NumberType.INT, 2, 0, 1, true, Direction.ASCENDING)));
    }

    @Test
    void descendsOnlyWhenTheEndIsLower() {
        assertEquals(Arrays.asList(2, 1, 0), values(new NumberValueSource<>(NumberType.INT, 2, 0, 1, true, Direction.DESCENDING)));
        assertEquals(Collections.emptyList(), values(new NumberValueSource<>(NumberType.INT, 0, 2, 1, true, Direction.DESCENDING)));
    }

    @Test
    void isASingleValueWhenBothBoundsAreEqual() {
        assertEquals(Collections.singletonList(5), values(new NumberValueSource<>(NumberType.INT, 5, 5, 1)));
        assertEquals(Collections.emptyList(), values(new NumberValueSource<>(NumberType.INT, 5, 5, 1, false)));
    }

    @Test
    void rejectsANonPositiveStep() {
        assertThrows(IllegalArgumentException.class, () -> new NumberValueSource<>(NumberType.INT, 0, 5, 0));
        assertThrows(IllegalArgumentException.class, () -> new NumberValueSource<>(NumberType.INT, 0, 5, -1));
    }
}
