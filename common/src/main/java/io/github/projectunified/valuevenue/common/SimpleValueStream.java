package io.github.projectunified.valuevenue.common;

import io.github.projectunified.valuevenue.core.ValueSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * A {@link ValueSource} whose values are a fixed list of strings, exposed as a single variable.
 */
public class SimpleValueStream implements ValueSource<UUID, String> {
    /**
     * The key used when none is given.
     */
    public static final String DEFAULT_KEY = "current_value";

    private final String key;
    private final List<String> values;

    /**
     * Creates the stream with {@link #DEFAULT_KEY}.
     *
     * @param values the values to expose
     */
    public SimpleValueStream(Collection<String> values) {
        this(DEFAULT_KEY, values);
    }

    /**
     * Creates the stream.
     *
     * @param key    name of the variable holding the value
     * @param values the values to expose
     */
    public SimpleValueStream(String key, Collection<String> values) {
        this.key = key;
        this.values = new ArrayList<>(values);
    }

    @Override
    public Object replace(String input, String value) {
        return input.equals(key) ? value : null;
    }

    @Override
    public Stream<String> getSourceValueStream() {
        return values.stream();
    }
}
