package io.github.projectunified.valuevenue.number;

import io.github.projectunified.valuevenue.core.ValueSource;

import java.util.stream.Stream;

/**
 * A {@link ValueSource} whose values are a range of numbers, with the start, the end and the step of the range
 * selected for each key.
 * <p>
 * The variable is {@link #DEFAULT_KEY} unless another key is given to the constructor, and resolves to the plain text
 * of the number.
 *
 * @param <K> type of the key
 * @param <N> type of the number
 */
public abstract class DynamicNumberValueSource<K, N extends Number> implements ValueSource<K, N> {
    /**
     * The key used when none is given.
     */
    public static final String DEFAULT_KEY = "current_value";

    private final NumberType<N> type;
    private final String key;
    private final boolean maxInclusive;
    private final Direction direction;

    /**
     * Creates the source with {@link #DEFAULT_KEY}, including the end.
     *
     * @param type the number type of the values
     */
    protected DynamicNumberValueSource(NumberType<N> type) {
        this(type, DEFAULT_KEY, true, Direction.AUTO);
    }

    /**
     * Creates the source.
     *
     * @param type         the number type of the values
     * @param key          name of the variable holding the number itself
     * @param maxInclusive whether the end is a value of the range
     * @param direction    the direction the values step in
     */
    protected DynamicNumberValueSource(NumberType<N> type, String key, boolean maxInclusive, Direction direction) {
        this.type = type;
        this.key = key;
        this.maxInclusive = maxInclusive;
        this.direction = direction;
    }

    /**
     * The first value of the range of the key.
     *
     * @param key the key
     * @return the first value
     */
    protected abstract Number getStart(K key);

    /**
     * The last value of the range of the key, or its bound when the end is not included.
     *
     * @param key the key
     * @return the last value
     */
    protected abstract Number getEnd(K key);

    /**
     * The distance between two values of the range of the key, positive.
     *
     * @param key the key
     * @return the step
     */
    protected abstract Number getStep(K key);

    @Override
    public Stream<N> getSourceValueStream(K key) {
        return type.values(getStart(key), getEnd(key), getStep(key), maxInclusive, direction);
    }

    @Override
    public Object replace(String input, N value) {
        return input.equals(key) ? type.format(value) : null;
    }
}
