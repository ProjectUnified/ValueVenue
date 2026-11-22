package io.github.projectunified.valuevenue.common;

import io.github.projectunified.valuevenue.core.ValueSource;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * A {@link ValueSource} whose source values are all constants of an enum, exposed as a single variable.
 * <p>
 * The variable is {@link #DEFAULT_KEY} unless another key is given to the constructor. Override
 * {@link #replace(String, Object)} to add more variables, calling the default for the rest.
 *
 * @param <K> type of the key
 * @param <SV> enum type of the source value
 */
public class EnumValueSource<K, SV extends Enum<SV>> implements ValueSource<K, SV> {
    /**
     * The key used when none is given.
     */
    public static final String DEFAULT_KEY = "current_value";

    private final String key;
    private final Class<SV> enumType;

    /**
     * Creates the source with {@link #DEFAULT_KEY}.
     *
     * @param enumType enum whose constants are the source values
     */
    public EnumValueSource(Class<SV> enumType) {
        this(DEFAULT_KEY, enumType);
    }

    /**
     * Creates the source.
     *
     * @param key      name of the variable holding the constant itself
     * @param enumType enum whose constants are the source values
     */
    public EnumValueSource(String key, Class<SV> enumType) {
        this.key = key;
        this.enumType = enumType;
    }

    @Override
    public Stream<SV> getSourceValueStream() {
        return Arrays.stream(enumType.getEnumConstants());
    }

    @Override
    public Object replace(String input, SV value) {
        return input.equals(key) ? value : null;
    }
}
