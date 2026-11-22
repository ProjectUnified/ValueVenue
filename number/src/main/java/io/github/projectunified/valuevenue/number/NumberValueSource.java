package io.github.projectunified.valuevenue.number;

import java.util.stream.Stream;

/**
 * A {@link io.github.projectunified.valuevenue.core.ValueSource} whose values are a fixed range of numbers, stepped
 * from the start to the end.
 * <p>
 * The variable is {@link #DEFAULT_KEY} unless another key is given to the constructor, and resolves to the plain text
 * of the number.
 *
 * @param <K> type of the key
 * @param <N> type of the number
 */
public class NumberValueSource<K, N extends Number> extends DynamicNumberValueSource<K, N> {
    private final Number start;
    private final Number end;
    private final Number step;

    /**
     * Creates the source with {@link #DEFAULT_KEY}, including the end.
     *
     * @param type  the number type of the values
     * @param start the first value
     * @param end   the last value, or the bound when it is not included
     * @param step  the distance between two values, positive
     */
    public NumberValueSource(NumberType<N> type, Number start, Number end, Number step) {
        this(type, start, end, step, true);
    }

    /**
     * Creates the source with {@link #DEFAULT_KEY}.
     *
     * @param type         the number type of the values
     * @param start        the first value
     * @param end          the last value, or the bound when it is not included
     * @param step         the distance between two values, positive
     * @param maxInclusive whether the end is a value of the range
     */
    public NumberValueSource(NumberType<N> type, Number start, Number end, Number step, boolean maxInclusive) {
        this(type, start, end, step, maxInclusive, Direction.AUTO);
    }

    /**
     * Creates the source with {@link #DEFAULT_KEY}.
     *
     * @param type         the number type of the values
     * @param start        the first value
     * @param end          the last value, or the bound when it is not included
     * @param step         the distance between two values, positive
     * @param maxInclusive whether the end is a value of the range
     * @param direction    the direction the values step in
     */
    public NumberValueSource(NumberType<N> type, Number start, Number end, Number step, boolean maxInclusive, Direction direction) {
        this(type, DEFAULT_KEY, start, end, step, maxInclusive, direction);
    }

    /**
     * Creates the source.
     *
     * @param type         the number type of the values
     * @param key          name of the variable holding the number itself
     * @param start        the first value
     * @param end          the last value, or the bound when it is not included
     * @param step         the distance between two values, positive
     * @param maxInclusive whether the end is a value of the range
     * @param direction    the direction the values step in
     */
    public NumberValueSource(NumberType<N> type, String key, Number start, Number end, Number step, boolean maxInclusive, Direction direction) {
        super(type, key, maxInclusive, direction);
        if (step.doubleValue() <= 0) {
            throw new IllegalArgumentException("The step must be positive, not " + step);
        }
        this.start = start;
        this.end = end;
        this.step = step;
    }

    @Override
    public Stream<N> getSourceValueStream() {
        // the range is the same for every key, so it is also the key-independent stream
        return getSourceValueStream(null);
    }

    @Override
    protected Number getStart(K key) {
        return start;
    }

    @Override
    protected Number getEnd(K key) {
        return end;
    }

    @Override
    protected Number getStep(K key) {
        return step;
    }
}
