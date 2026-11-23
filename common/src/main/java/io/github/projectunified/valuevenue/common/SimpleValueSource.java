package io.github.projectunified.valuevenue.common;

import io.github.projectunified.valuevenue.core.ValueSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A {@link ValueSource} whose values are a fixed list of values, exposed as a single variable.
 * <p>
 * The variable resolves to the formatted value, while {@link #getSourceValueStream()} exposes the values as they are,
 * so the formatter only decides what a template sees.
 *
 * @param <K> type of the key
 * @param <V> type of the value
 */
public class SimpleValueSource<K, V> implements ValueSource<K, V> {
    /**
     * The key used when none is given.
     */
    public static final String DEFAULT_KEY = "current_value";

    private final String key;
    private final List<V> values;
    private final Function<V, Object> formatter;

    /**
     * Creates the source with {@link #DEFAULT_KEY}.
     *
     * @param values    the values to expose
     * @param formatter the formatting of a value to the replacement of the variable
     */
    public SimpleValueSource(Collection<V> values, Function<V, Object> formatter) {
        this(DEFAULT_KEY, values, formatter);
    }

    /**
     * Creates the source.
     *
     * @param key       name of the variable holding the value
     * @param values    the values to expose
     * @param formatter the formatting of a value to the replacement of the variable
     */
    public SimpleValueSource(String key, Collection<V> values, Function<V, Object> formatter) {
        this.key = key;
        this.values = new ArrayList<>(values);
        this.formatter = formatter;
    }

    @Override
    public Object replace(String input, V value) {
        return input.equals(key) ? formatter.apply(value) : null;
    }

    @Override
    public Stream<V> getSourceValueStream() {
        return values.stream();
    }
}
