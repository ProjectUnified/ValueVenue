package io.github.projectunified.valuevenue.common;

import io.github.projectunified.valuevenue.core.ValueSource;

import java.util.Arrays;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * A {@link ValueSource} whose values are the pieces of a string, e.g. the lines of a setting.
 * <p>
 * The string is selected for each key by the source function and cut by the split pattern; a key without a string has
 * no value. The variable is {@link #DEFAULT_KEY} unless another key is given to the constructor, and resolves to the
 * piece itself.
 *
 * @param <K> type of the key
 */
public class StringSplitSource<K> implements ValueSource<K, String> {
    /**
     * The key used when none is given.
     */
    public static final String DEFAULT_KEY = "current_value";

    /**
     * The split pattern used when none is given: one value per line.
     */
    public static final Pattern DEFAULT_SPLIT_PATTERN = Pattern.compile("\\r?\\n");

    private final String key;
    private final Function<K, String> sourceFunction;
    private final Pattern splitPattern;

    /**
     * Creates the source with {@link #DEFAULT_KEY} and {@link #DEFAULT_SPLIT_PATTERN}.
     *
     * @param sourceFunction the function selecting the string of a key
     */
    public StringSplitSource(Function<K, String> sourceFunction) {
        this(sourceFunction, DEFAULT_SPLIT_PATTERN);
    }

    /**
     * Creates the source with {@link #DEFAULT_KEY}.
     *
     * @param sourceFunction the function selecting the string of a key
     * @param splitPattern   the pattern the string is cut by
     */
    public StringSplitSource(Function<K, String> sourceFunction, Pattern splitPattern) {
        this(DEFAULT_KEY, sourceFunction, splitPattern);
    }

    /**
     * Creates the source with {@link #DEFAULT_SPLIT_PATTERN}.
     *
     * @param key            name of the variable holding the piece itself
     * @param sourceFunction the function selecting the string of a key
     */
    public StringSplitSource(String key, Function<K, String> sourceFunction) {
        this(key, sourceFunction, DEFAULT_SPLIT_PATTERN);
    }

    /**
     * Creates the source.
     *
     * @param key            name of the variable holding the piece itself
     * @param sourceFunction the function selecting the string of a key
     * @param splitPattern   the pattern the string is cut by
     */
    public StringSplitSource(String key, Function<K, String> sourceFunction, Pattern splitPattern) {
        this.key = key;
        this.sourceFunction = sourceFunction;
        this.splitPattern = splitPattern;
    }

    @Override
    public Stream<String> getSourceValueStream(K key) {
        String source = sourceFunction.apply(key);
        return source == null ? Stream.empty() : Arrays.stream(splitPattern.split(source));
    }

    @Override
    public Object replace(String input, String value) {
        return input.equals(key) ? value : null;
    }
}
