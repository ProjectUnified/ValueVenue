package io.github.projectunified.valuevenue.common;

import io.github.projectunified.valuevenue.core.ValueSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * A {@link ValueSource} whose values are maps, exposed as the {@code <key>_<name>} variables.
 */
public class KeyValueStream implements ValueSource<UUID, Map<String, Object>> {
    /**
     * The key used when none is given.
     */
    public static final String DEFAULT_KEY = "key";

    private final String prefix;
    private final List<Map<String, Object>> values;

    /**
     * Creates the stream with {@link #DEFAULT_KEY}.
     *
     * @param values the maps to expose
     */
    public KeyValueStream(Collection<? extends Map<String, Object>> values) {
        this(DEFAULT_KEY, values);
    }

    /**
     * Creates the stream.
     *
     * @param key    prefix of the variables, without the trailing underscore
     * @param values the maps to expose
     */
    public KeyValueStream(String key, Collection<? extends Map<String, Object>> values) {
        this.prefix = key + "_";
        this.values = new ArrayList<>(values);
    }

    @Override
    public Object replace(String input, Map<String, Object> value) {
        if (!input.startsWith(prefix)) {
            return null;
        }
        return value.getOrDefault(input.substring(prefix.length()), "");
    }

    @Override
    public Stream<Map<String, Object>> getSourceValueStream() {
        return values.stream();
    }
}
